import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.charset.StandardCharsets;



public class Slave {

    static int numMachines = 8; // Number of machines
    static ArrayList<ArrayList<String>> arraysToProcess = new ArrayList<ArrayList<String>>();
    static int countArrayReceived = 0;
    static ServerSocket serverSocket = null;
    static Socket socket = null;
    


    public static void main(String[] args) {
       

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
        
        
        ArrayList<String> wordList = null;
        
        int port = 12509; // Replace with the actual listening port number
        ExecutorService executorService = Executors.newFixedThreadPool(numMachines+1);

        try {
            serverSocket = new ServerSocket(12345);

        
            socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            byte[] receivedBytes = receiveByteArray(inputStream);
            String receivedString = new String(receivedBytes, StandardCharsets.UTF_8);
           // System.out.println(receivedString);
            wordList = processReceivedString(receivedString);

 


            
            
        // Assign each word to a machine ID
        ArrayList<ArrayList<String>> wordAssignments = new ArrayList<>();
        for (int i = 0; i < numMachines; i++) {
            wordAssignments.add(new ArrayList<>());
        }
        for (String word : wordList) {
            // Compute the hash value using the built-in Java hash function
            int machineId = Math.abs(word.hashCode()) % numMachines;

            wordAssignments.get(machineId).add(word);
        }

            ServerSocket serverSocket2= new ServerSocket(port);
            for (int i = 0; i < numMachines; i++) {
                executorService.submit(() -> receiveWordArray(serverSocket2));
            }
      

        OutputStream outputStream = socket.getOutputStream();
        PrintWriter out = new PrintWriter(outputStream, true);
   
        out.println("finish");

        try {
            
            InputStream inputStream2 = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream2));
            String inputString = reader.readLine();
            if (inputString.equals("start")) {
                System.out.println("start shuffle");

                
        
            for (int i = 0; i < numMachines; i++) {
            // Send the arrays to the corresponding machines via TCP sockets
                String currentMachine = machineMap.get(i + 1);
                ArrayList<String> wordArray = wordAssignments.get(i);
                executorService.submit(() -> sendArrayToMachine(wordArray, currentMachine, port));
    
            }

            
            
        }
        
        else{
                System.out.println("Error: didn't receive start message.");
        }

               
         executorService.shutdown();

        
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }catch(IOException e) {
            e.printStackTrace();
    }

}
    
    

    private static byte[] receiveByteArray(InputStream inputStream) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String data = new String(buffer, 0, bytesRead);
                if (data.contains("EndOfStream")) {
                   // System.out.println(data);
                    break;
                }
               // System.out.println(InetAddress.getLocalHost().getHostName()+"bytes read = "+bytesRead);
                byteStream.write(buffer, 0, bytesRead);
                //System.out.println(data);
               
                
                // Process the data
                
                
            }
            System.out.println("Array received from the master");
            return byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<String> processReceivedString(String receivedString) {
        ArrayList<String> wordList = new ArrayList<>();
        String[] lines = receivedString.split("\n");
        for (String line : lines) {
            String[] words = line.trim().split("\\s+");
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (!word.isEmpty()) {
                    wordList.add(word);
                }
            }
        }
        return wordList;
    }


    private static void sendArrayToMachine(ArrayList<String> wordArray, String machine, int port) {
            System.out.println("entered method sendArraytoMachine");
            try {

                
                //Define the machine's host and port
                String host = machine; // Replace with the actual host of the machine
                

                //Create a TCP socket connection to the machine
                Socket socket = new Socket(host, port);

                //Open streams for sending the word array
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
               // System.out.println("sending " + wordArray + "to "+ host + "from  " + InetAddress.getLocalHost().getHostName());
                //Send the word array to the socket output stream
                objectOutputStream.writeObject(wordArray);
                
                
                //Close the streams and socket
                objectOutputStream.close();

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    private static void receiveWordArray(ServerSocket serverSocket) {

        System.out.println("entered method receivedWordArray");
        ArrayList<String> arrayReceived = null;

        try {
            
            //Accept a connection from the Master

            Socket socket = serverSocket.accept();
            System.out.println("accepted connection");
            
            //Open streams for receiving the word array
            InputStream inputStream = socket.getInputStream();
            System.out.println("getinputstream");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            

            //Read the word array from the socket input stream
            
            arrayReceived = (ArrayList<String>)objectInputStream.readObject();
            arraysToProcess.add(countArrayReceived, arrayReceived);
           // System.out.println("array added to process");
        //   System.out.println(arraysToProcess);
            countArrayReceived = countArrayReceived +1;
            //System.out.println(countArrayReceived);
            //System.out.println(InetAddress.getLocalHost().getHostName() + "recieved " + arrayReceived);   
            
            if(countArrayReceived == numMachines){
                processReceivedWordArray(arraysToProcess);
                
            }

            //Close the streams and socket
            objectInputStream.close();
           
            //processReceivedWordArray(arrayReceived);

            
            socket.close();
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void processReceivedWordArray(ArrayList<ArrayList<String>> receivedArray) {
        //Process the received word array
        HashMap<String, Integer> wordCountMap = new HashMap<>();
        for( ArrayList<String> array :receivedArray ){
            if (array != null) {
                if(!(array.isEmpty())){    

                    //Count the occurrences of each word in the received word array
                    for (String word : array) {
                        wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                    }
                }
            }
                    //Print the word count results   
            
        }
        try{
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outputStream, true);

            for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
                
                    String word = entry.getKey();
                    int count = entry.getValue();
                    //System.out.println("Word: " + word + ", Count: " + count);
                    
                    out.println("Word: " + word + ", Count: " + count);
            }

            out.close();

            socket.close();
            serverSocket.close();

        }catch(Exception e){
            e.printStackTrace();
        }

        
        
               
    }

   
    
}


