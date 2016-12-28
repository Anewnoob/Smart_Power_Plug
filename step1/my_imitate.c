#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <string.h>
#include <assert.h>
#include <wait.h>
#include <netinet/in.h>
#include <sys/stat.h>
#include <fcntl.h>
#define ADC_SET_CHANNEL		0xc000fa01


double current=0;              //电流
double voltage=0;              //电压
char recvbuf[1];                   //一位0/1控制开关  一位'\0'   
char sendbuf[2];                  //电流，电压数据      

int main()
{
	int client_sockfd;
	struct sockaddr_in remote_addr;    //客服端Socket
	
	memset(&remote_addr,0,sizeof(remote_addr)); 
	remote_addr.sin_family=AF_INET;
	remote_addr.sin_addr.s_addr=inet_addr("120.24.12.197");
	remote_addr.sin_port=htons(10001); 
	
	if((client_sockfd=socket(AF_INET,SOCK_STREAM,0))<0)
	{
		perror("socket");
		return 1;
	}
	int connected = -1;
	do
	{
		connected = connect(client_sockfd,(struct sockaddr *)&remote_addr,sizeof(struct sockaddr));
		sleep(2);
	}while(connected<0);
	printf("connected to server/n");

	
	pid_t child;
	if((child = fork()) == -1)
	{
		perror("fork error");
		exit(EXIT_FAILURE);
	}
	else if(child == 0)//接收端
	{
		while(1)
		{
			//打开设备
			fd = open("/dev/leds", 0);
			recv(client_sockfd,recvbuf,strlen(recvbuf),0);
				if(recvbuf[0]=='1')
				{
					if(ioctl(fd, 1, 4)!=0)
					printf("开启插座失败！");
					//exec(leds/led 4 1);
				}
				else if(recvbuf[0]=='2')
				{
					if(ioctl(fd, 0, 4)!=0); 
					printf("关闭插座失败！");
					//exec(leds/led 4 0);
				}
			memset(recvbuf, 0, sizeof(recvbuf));		
		}
	}
	else	//发送端
	{
		while(1)
		{
			int fd,len,value;
			char buffer[30];
			
			//打开设备
			fd = open("/dev/adc", 0);
			
			//读电压
			ioctl(fd, ADC_SET_CHANNEL, 0);
			len = read(fd, buffer, sizeof buffer -1);
			if (len > 0)
			{
				buffer[len] = '\0';
				value = -1;
				sscanf(buffer, "%d", &value);
				printf("U = %d ", value);          
				voltage = value; 
			}
			else 
			{
				perror("read voltage fail:");
				return 1;
			}
			memset(buffer, 0, sizeof(buffer));  //清空
			
			//读电流
			ioctl(fd, ADC_SET_CHANNEL, 1);
			len = read(fd, buffer, sizeof buffer -1);
			if (len > 0)
			{
				buffer[len] = '\0';
				value = -1;
				sscanf(buffer, "%d", &value);
				printf("I = %d\n", value);
				current = value;
			}
			else 
			{
				perror("read current fail:");
				return 1;
			}
			sprintf(sendbuf, "%lf",voltage);
			sprintf(sendbuf, "%lf",current);
			sendbuf[3]='\0';
			send(client_sockfd,sendbuf,strlen(sendbuf),0);
			sleep(60);
		}
	}
	close(client_sockfd);
    return 0;
}
