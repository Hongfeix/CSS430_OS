/*
* @file: processes.cpp
* @author: Hongfei xie 
* @description: execute ps -A | grep argv | wc -l
*/
#include<iostream>
#include<unistd.h>
#include<stdlib.h>
#include<stdio.h>
#include<sys/wait.h>
using namespace std;

int main(int argc, char **argv){
  enum{READ,WRITE};
  
  int pid  =  fork();  //  create  child  process

  int fd1[2];    //  fd  for  first  pipe
  int fd2[2];    //  fd  for  second  pipe
  int rc1  =  pipe(fd1);
  int rc2  =  pipe(fd2);

  if  (argc  <  2){  //  input  check
    perror("Too  few  arguments");
    exit(EXIT_FAILURE);
  }
  if  (rc1  <  0  ||  rc2  <  0){  //  arugment  error
    perror("Error  creating  the  pipe");
    exit(EXIT_FAILURE);
  }    

  if  (pid  ==  0){
    int  pid1  =  fork();  //  create  grand-child  process

    if  (pid1  ==  0){        //  in  grand-child
      int  pid2  =  fork();  //  create  grand-grand-child  process

      if  (pid2  ==  0){        //  grand-grand-child  process
        close(fd1[READ]);  //  close  read  pipe  between  child  and  grand-child
        close(fd1[WRITE]);//  close  write  pipe  between  child  and  grand-child
        close(fd2[READ]);  //  close  read  pipe  between  grand-grand-child  and  grand-child
        dup2(fd2[WRITE],1);  //  write  the  execution  result  to  pipe  between  grand-grand-child  and  grand-child
        execlp("ps","ps","-A",NULL);  //  execute  first  command
      }

      else{  //  grand-child  process
        close(fd1[READ]);    //  close  read  pipe  between  child  and  grand-child
        close(fd2[WRITE]);  //  close  write  pipe  between  grand-grand-child  and  grand-child
        wait(NULL);  //  wait  for  grand-grand-child  finish
        dup2(fd2[READ],0);  //  read  from  pipe  between  grand-grand-child  and  grand-child
        dup2(fd1[WRITE],1);//  write  execution  result  to  pipe  between  child  and  grand-child
        execlp("grep","grep",argv[1],NULL);  //  execute  middle  command
      }
    }

    else{  //  child  process
      close(fd1[WRITE]);  //  close  write  pipe  between  child  and  grand-child
      close(fd2[READ]);    //  close  read  pipe  between  grand-child  and  grand-grand-child
      close(fd2[WRITE]);  //  close  write  pipe  between  grand-child  and  grand-grand-child
      wait(NULL);  //  wait  for  grand-child  finish
      dup2(fd1[READ],0);  //  write  the  exectuion  result  to  pipe  between  child  and  grand-child
      execlp("wc","wc","-l",NULL);  //  execute  last  command  
      }
    }

  else{  //  parent  process  (doing  nothing  but  wait  for  derived  process  finished)
    wait(NULL);  //  wait  for  child  finish
  }
  return  0;
}    
