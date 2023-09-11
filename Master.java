import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Master {

    static int count =0; 
    static int numMachines = 8; // Number of machines
    static int numTasks = numMachines;
    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(numTasks);
        String inputFilePath = "sante_publique.txt";
        ExecutorService executorService = Executors.newFixedThreadPool(numMachines);
        

       
        HashMap<Integer, String> machineMap = new HashMap<>(); // HashMap to assign machines to numbers

         // Add machines to the HashMap
      
        machineMap.put(1, "tp-5b07-26.enst.fr");
        machineMap.put(2, "tp-1d23-13.enst.fr");
        machineMap.put(3, "tp-5b01-31.enst.fr");
        machineMap.put(4, "tp-3a107-19.enst.fr");
        machineMap.put(5, "tp-5b01-22.enst.fr");
        machineMap.put(6, "tp-5b01-25.enst.fr");
        machineMap.put(7, "tp-3c41-06.enst.fr");
        machineMap.put(8, "tp-1a222-02.enst.fr");
        

        try {
        // Read the input file
        byte[] fileData = Files.readAllBytes(Path.of(inputFilePath));
        String fileContent = new String(fileData, StandardCharsets.UTF_8);
        String[] words = fileContent.split("\\s+");  // Split the content into words

        int numWords = words.length;
        int chunkSize = (int) Math.ceil((double) numWords / numMachines);
        byte[][] subarrays = new byte[numMachines][];

        Future<?>[] futures = new Future[numMachines];
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numMachines; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, numWords);
            String[] subarrayWords = Arrays.copyOfRange(words, start, end);
            String concatenatedWords = String.join(" ", subarrayWords);
            byte[] subarrayData = concatenatedWords.getBytes(StandardCharsets.UTF_8);
            subarrays[i] = subarrayData;

            // Get the machine name for the current iteration
            String machineName = machineMap.get(i + 1);
            int j=i;

            // Send the byte array to the machine
            futures[i] = executorService.submit(new Task(machineName,subarrays[j], latch));
        }

        
        try{

            latch.await();
            System.out.println("All executor services are closed.");
            long endTime = System.currentTimeMillis();
            long exTime = endTime - startTime;
            System.out.println(exTime);

            executorService.shutdown();

        }catch(InterruptedException e){
            e.printStackTrace();
        }

        
    } catch (IOException e) {
        // Handle file read error
        e.printStackTrace();
    }

    
    
        
        


        
    }

    private static void sendByteArrayToMachine(String machineName, byte[] data) {
        
        try (Socket socket = new Socket(machineName, 12345)) { // Replace 8080 with the appropriate port number
            
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data);
            PrintWriter out = new PrintWriter(outputStream, true);
            out.println("EndOfStream");
            //out.close();
    
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String inputString = reader.readLine();
            System.out.println("sending array to : "+ machineName);
            if(inputString.equals("finish")){
                count = count+1;
            }

            while(count != numMachines){try {
                Thread.sleep(3000);
            }
            catch(Exception e){

            }
            }

            System.out.println("sending start msg to machines");
            String mess = "start";
            OutputStream outputStream2 = socket.getOutputStream();
            PrintWriter out2 = new PrintWriter(outputStream2, true);
            out2.println(mess);

            InputStream inputStream2 = socket.getInputStream();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2));

            String message;
            while ((message = reader2.readLine()) != null) {
                System.out.println(message);
            }
            reader.close();
            

         
            //out2.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     static class Task implements Runnable {
        String machineName = null;
        byte[] data =null;
        private final CountDownLatch latch;

        public Task(String machineName, byte[] data, CountDownLatch latch){
            this.machineName = machineName;
            this.data = data;
            this.latch = latch;

        }
        @Override
        public void run() {
            sendByteArrayToMachine(machineName, data);
            latch.countDown();
        }
    }




}
