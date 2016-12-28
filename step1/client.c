#include <stdio.h>
#include <time.h>
#include <string.h>
#include<unistd.h>
#include <stdio.h>
#include<sys/types.h>
#include<sys/wait.h>
#include<unistd.h>
#include<assert.h>
#include<stdlib.h>
#include<pthread.h>
#include <wait.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#define Number_Equ 4
#define BOOL int
#define MAXN_STR 40
#define ADC_SET_CHANNEL		0xc000fa01

//定义插座与每个供电插口结构体///
typedef struct 
{
   int   state;            //状态
   double current;              //电流
   double voltage;              //电压
   
}Electrical_Equipment;

typedef struct 
{
   int number;
   Electrical_Equipment   EEq[Number_Equ];   //结构数组
}Socket,*PSocket;



//函数声明//
BOOL Write_txt();
BOOL Init_Socket(PSocket);
BOOL Read_txt(PSocket);

int count=0;
pthread_cond_t count_ready;
pthread_mutex_t count_lock;
int nRead ,nSend;
int nWrite,nRecv;
int clientfd;
char sendBuf[1024]={0};  
char recvbuf[1024]={0};
FILE* fp1 = 0,*fp2 = 0;
Socket Tmp;
PSocket PTmp = &Tmp; 
int  flag_lock = 0;
int flag_openclose;
void* Rec(void* arg)
{
	pthread_mutex_trylock(&count_lock);
        if(pthread_cond_wait(&count_ready,&count_lock) == 0)
         {
           flag_lock = 1;
         }
        fp2=fopen("in2.txt","wb+");  //OPEN in2.txt  by WB+
        printf("Cline  Recing.....\n");
        nRecv = recv(clientfd, recvbuf,50, 0 );
        if( nRecv == -1 )  
        {  
            printf("recv\n"); 
            return;  
        } 
        nWrite=fwrite(recvbuf,50,1,fp2);   //write to in2.txt
        if( nWrite<1 && ferror(fp2)!=0 )  
        {  
            printf( "failed to write file\n" );  
            return;  
        } 
        fclose(fp2);
        printf("Cline  Rec OK\n");
        Read_txt(PTmp);  
        pthread_mutex_unlock(&count_lock);
}
void* Send(void* arg)
{
         
         pthread_mutex_lock(&count_lock);
         Init_Socket(PTmp);
         Write_txt(PTmp);
         fp1=fopen("out1.txt","rb");
         printf("Cline Sending...\n"); 
         while(1)
         {
           nRead = fread(sendBuf, 50, 1, fp1);
           if(nRead<1 && ferror(fp1)!=0)  
           {  
             printf("failed to read file\n");  
             return;  
           }  
           nSend = send(clientfd, sendBuf, 50, 0 );  
           if( nSend == -1 )  
           {  
             return;  
           }  
           if(feof(fp1))  
           {  
              break;  
           }  
         } 
         printf("Cline  Send OK\n");
         fclose(fp1);
         pthread_cond_signal(&count_ready);
	 pthread_mutex_unlock(&count_lock);
}



int main(int argc,char argv[])
{
 
   pthread_t tid1,tid2;
   int lenclient,lenserver;
   int cnt = 0;
   int i = 50000;
   int rc;
   char buff[MAXN_STR+1];
    /* server的套接字地址 */
   struct sockaddr_in serveraddr;
   struct sockaddr_in clientaddr;
   srand((unsigned)time(0));
   
   (PTmp -> EEq[0]).state = 0; //initial states 0
   pthread_mutex_init(&count_lock,NULL);
   pthread_cond_init(&count_ready,NULL);
   while(1)
   {
    /* 创建client的套接字并且初始化server和client的套接字地址 */
    clientfd = socket(AF_INET, SOCK_STREAM, 0);
    memset(&clientaddr,0,sizeof(clientaddr));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_port = htons(6035);
    serveraddr.sin_addr.s_addr = inet_addr("120.24.12.197");
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_port = htons(6035);
    clientaddr.sin_addr.s_addr = inet_addr("192.168.1.112");

    /* 绑定client套接字 */

if(bind(clientfd, (struct sockaddr*)&clientaddr, sizeof(clientaddr)) == 0)
     printf("bind ok\n");
else {printf("bind error\n");return 0;}
     printf("connectng...\n");
     do
     {
       rc = connect(clientfd, (struct sockaddr*)&serveraddr, sizeof(serveraddr));
     }while(rc != 0);        
     printf("connect ok\n");
     pthread_create(&tid2,NULL,Rec,NULL);
     usleep(50000); 
     pthread_create(&tid1,NULL,Send,NULL);
     pthread_join(tid2,NULL);
     pthread_join(tid1,NULL); 
     shutdown(clientfd,2);
     close(clientfd);
     sleep(1);
   }
   return 0; 
}


////////////////////////
BOOL Write_txt(PSocket PTmp)
{
   int i;
   FILE *fp;
   fp = fopen("out1.txt","w+");    //send
   for(i = 0; i< 1 ;i++)
   {
      fprintf(fp,"receive\n");
      fprintf(fp,"%d\n",(PTmp -> EEq[i]).state);
      fprintf(fp,"%lf\n",(PTmp -> EEq[i]).voltage);
      fprintf(fp,"%lf\n",(PTmp -> EEq[i]).current);
      fprintf(fp,"over\n");
   }
   fclose(fp);//关闭文件	
}
BOOL Read_txt(PSocket PTmp)
{

   int fd;
   FILE *fp;
   char TempS[20];
   fd = open("/dev/leds", 0);
   fp = fopen("in2.txt","r+");
   fscanf(fp,"%s",TempS);

   if(strcmp("true",TempS) == 0)
   {	
        (PTmp -> EEq[0]).state = 1; 
        ioctl(fd, 1, 4);
	printf("现在状态为: open\n");
   }
   else if(strcmp("false",TempS) == 0)
   {
        (PTmp -> EEq[0]).state = 0; 
        ioctl(fd, 0, 4);       
        printf("现在状态为: close\n");
   }
   close(fd); 
   fclose(fp);//关闭文件	
}
BOOL Init_Socket(PSocket PTmp)
{
   int tmp_state = 1;
   int fd, n, len, value;
   char buffer[30];
   fd = open("/dev/adc", 0);
   PTmp -> number = 1;
   
   ioctl(fd, ADC_SET_CHANNEL, 0);
   len = read(fd, buffer, sizeof buffer -1);  
   if (len > 0)
   {
	buffer[len] = '\0';
	value = -1;
	sscanf(buffer, "%d", &value);
	printf("U = %d ", value);
        (PTmp -> EEq[0]).voltage = value; 
   }
	ioctl(fd, ADC_SET_CHANNEL, 1);
	len = read(fd, buffer, sizeof buffer -1);
	if (len > 0)
	{
	    buffer[len] = '\0';
	    value = -1;
	    sscanf(buffer, "%d", &value);
	    printf("I = %d\n", value);
            (PTmp -> EEq[0]).current = value;
	}
        else 
	{
	    perror("read ADC device:");
	    return 1;
	}
		
	//usleep (1000) 
   return 1;
}

