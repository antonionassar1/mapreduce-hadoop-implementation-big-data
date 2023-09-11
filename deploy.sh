#!/bin/bash
# A simple variable example
login="obeid-22"
remoteFolder="/tmp/$login/"
fileName="Slave"
fileExtension=".java"
#tmp=$(cat machines.txt | tr "\n" " ")

computers=("tp-5b07-26.enst.fr" "tp-1d23-13.enst.fr" "tp-5b01-31.enst.fr" "tp-3a107-19.enst.fr" "tp-5b01-22.enst.fr" "tp-5b01-25.enst.fr" "tp-3c41-06.enst.fr" "tp-1a222-02.enst.fr")

#read -ra computers <<< "$tmp"
      

for c in ${computers[@]}; do
  command0=("ssh" "$login@$c" "lsof -ti | xargs kill -9")
  command1=("ssh" "$login@$c" "rm -rf $remoteFolder;mkdir $remoteFolder")
  command2=("scp" "$fileName$fileExtension" "$login@$c:$remoteFolder$fileName$fileExtension")
  command3=("ssh" "$login@$c" "cd $remoteFolder;javac $fileName$fileExtension;java $fileName")
  echo ${command0[*]}
  "${command0[@]}"
  echo ${command1[*]}
  "${command1[@]}"
  echo ${command2[*]}
  "${command2[@]}"
  echo ${command3[*]}
  "${command3[@]}" &
done