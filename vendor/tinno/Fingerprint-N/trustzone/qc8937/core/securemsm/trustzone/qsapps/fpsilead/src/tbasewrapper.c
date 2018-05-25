#include "../inc/tbasewrapper.h"
#include "stdio.h"
#include <stdarg.h>
#include "qsee_spi.h"
#include "qsee_log.h"
#include <signal.h>
#include "qsee_hmac.h"

#ifdef __cplusplus
extern "C"
{
#endif

typedef enum
{
		QSEE_GPIO_LOW,
		QSEE_GPIO_HIGH,
}qsee_gpio_value_t;

#define DELAY_BASE_NUMBER   720000
#define LEVEL_DEBUG   4
#define LEVEL_ERROR   5

#define FP_SPI_DEVICE_ID QSEE_SPI_DEVICE_6
//#define FP_HUAWEI

static  qsee_spi_config_t    g_spi_config;
static  qsee_spi_transaction_info_t g_write_info;
static  qsee_spi_transaction_info_t g_read_info;  
static int g_has_init = 0;

extern  int sl_log_printf(int prio,  const char *fmt, ...);

extern int qsee_tlmm_gpio_id_out(unsigned int gpio_id, int output_val);
extern int open(const char *name, int flags, ...);
extern off_t lseek(int fdno, off_t offset, int whence);
extern int close(int fdno);
extern void *malloc(size_t size);
extern void free(void *mem);
extern unsigned  long long qsee_get_uptime();

extern void *qsee_malloc(unsigned int size);
extern void qsee_free(void* ptr);

#define  QSEE_HMAC_KEY_MAX_LENGTH 32
static unsigned  char hmackey[QSEE_HMAC_KEY_MAX_LENGTH] = {0};
static unsigned int hmackeylen = 0;
static int slfp_SpiDev_Handle = 0;

void  slfp_setHmacKey(unsigned  char  *key,unsigned  int  keylen)
{
     hmackeylen = keylen;
     if(keylen<=QSEE_HMAC_KEY_MAX_LENGTH)
     {
        memcpy(hmackey,key,keylen);
     }
}

int  slfp_ComputeSignature(unsigned  char  *signature, unsigned int signature_length,void *msgHead,unsigned int msgLen)
{
     int  status = -1;
     if(hmackeylen>0)
     {
       status = qsee_hmac(QSEE_HMAC_SHA256,
                msgHead,
                msgLen,
                hmackey,
                hmackeylen,
                signature);
     }
     return  status;
}

int silead_open(const char *name, int flags, ...)
{
	return open(name,flags);
}

off_t silead_lseek(int fdno, off_t offset, int whence)
{
	return lseek(fdno, offset, whence);
}

int silead_close(int fdno)
{
	return close(fdno);
}

void   silead_qsee_log(const  char  *buf)
{
     //qsee_log_mask = qsee_log_get_mask();
     //qsee_log_set_mask(QSEE_LOG_MSG_ERROR | QSEE_LOG_MSG_FATAL | QSEE_LOG_MSG_DEBUG);
     qsee_log(QSEE_LOG_MSG_DEBUG,"%s",buf);
}

void  slfp_HWClockOn()
{
    if(slfp_SpiDev_Handle == 0)
    {
        int   ret;
        int   i;
        for(i = 3;i>0;i--)
        {
            ret = qsee_spi_open(FP_SPI_DEVICE_ID);
            if(ret==0)
            {
                slfp_SpiDev_Handle = 1;
                break;
            }
            else
            {
                slfp_Delay(2);
                qsee_spi_close(FP_SPI_DEVICE_ID);
                slfp_Delay(2);
            }
        }
    }
}

void  slfp_HWClockOff()
{
    if(slfp_SpiDev_Handle)
    {
    	qsee_spi_close(FP_SPI_DEVICE_ID);
        slfp_SpiDev_Handle = 0;
    }
}

int slfp_HWGpioLevelGet()
{
	return 0;
}


void slfp_HWSpiDev_Init(void)
{
	sl_log_printf(LEVEL_DEBUG, "slfp_HWSpi_Init enter");

    if(slfp_SpiDev_Handle)
    {
    	qsee_spi_close(FP_SPI_DEVICE_ID);
        slfp_SpiDev_Handle = 0;
    }
    int   ret;
    int   i;
    for(i = 3;i>0;i--)
    {
        ret = qsee_spi_open(FP_SPI_DEVICE_ID);
        if(ret==0)
        {
            slfp_SpiDev_Handle = 1;
            break;
        }
        else
        {
            slfp_Delay(2);
            qsee_spi_close(FP_SPI_DEVICE_ID);
            slfp_Delay(2);
        }
    }

    if(slfp_SpiDev_Handle)
    {
	    if(!g_has_init)
	    {
		    GslSpiConfig(NULL);
	    }
    }
}

void slfp_HWSpiDev_DeInit()
{
    if(slfp_SpiDev_Handle)
    {
    	qsee_spi_close(FP_SPI_DEVICE_ID);
        slfp_SpiDev_Handle = 0;
    }
}

int GslSpiConfig(spi_conf_t * pSpi_config)
{
      g_spi_config.spi_clk_always_on = QSEE_SPI_CLK_NORMAL;
	  g_spi_config.spi_clk_polarity =  QSEE_SPI_CLK_IDLE_LOW;
	  g_spi_config.spi_cs_mode =       QSEE_SPI_CS_KEEP_ASSERTED;
	  g_spi_config.spi_cs_polarity =   QSEE_SPI_CS_ACTIVE_LOW;
	  g_spi_config.spi_shift_mode =    QSEE_SPI_INPUT_FIRST_MODE;
	  g_spi_config.hs_mode = 0;
	  g_spi_config.loopback = 0;
      g_spi_config.spi_slave_index = 0;
      g_spi_config.deassertion_time_ns = 0;
	  if(pSpi_config !=NULL)
	  {
            if(pSpi_config->Bits_per_word!=-1)
            {
                  g_spi_config.spi_bits_per_word = pSpi_config->Bits_per_word;
            }
            else
            {
                  g_spi_config.spi_bits_per_word = 8;
            }

            if(pSpi_config->Speed!=-1)
            {
                  g_spi_config.max_freq = pSpi_config->Speed;
            }
            else
            {
                  g_spi_config.max_freq = 2000000;
            }
	  }
	  else
	  {
		  g_spi_config.spi_bits_per_word = 8;
		  g_spi_config.max_freq = 2000000;	  
	  }
	  g_has_init = 1;
   	return 0;
}

//static unsigned char *tmpBuffer = NULL;


int slfp_spiDuplexCommunication(void *p_write_info, void* p_read_info)
{
	int result = 0;
	if(!p_write_info || !p_read_info)
	{
			return -1;
	}
	
	result = qsee_spi_full_duplex(FP_SPI_DEVICE_ID, &g_spi_config, (qsee_spi_transaction_info_t *)p_write_info, (qsee_spi_transaction_info_t *)p_read_info);
	return result;
} 

static int __attribute__((unused)) slfp_reg_init(unsigned char addr)
{
	int error = 0;
	qsee_spi_transaction_info_t read_data;
	qsee_spi_transaction_info_t write_data;
	unsigned char  reg_buf[2] = {addr, 0};
	unsigned char  rx_buf[7] = {0};
	write_data.buf_addr = reg_buf;
	write_data.buf_len = 2;
	
	read_data.buf_addr = rx_buf;
	read_data.buf_len = 7;
	error = qsee_spi_full_duplex(FP_SPI_DEVICE_ID, &g_spi_config, &write_data, &read_data);
	if(error)
	{
		sl_log_printf(LEVEL_ERROR, "slfp_reg_init SPI error = %d", error);
		error = -1;
	}
	
	return error;
}

void slfp_Delay(int msecs)
{
    volatile int __attribute__ ((unused)) delay_value = 1;
    unsigned  long long  beginTime = qsee_get_uptime();
    unsigned  long long  currentTime;
    while(1)
    {
          for(int i=0;i<1000;i++)
          {
              delay_value = 0;
          }
          currentTime = qsee_get_uptime();
          if(currentTime>= beginTime+msecs)
          {
               break;
          }
    }
}

#ifdef FP_HUAWEI

int __aeabi_idiv0(void)
{
	return raise(SIGFPE);
}

int __aeabi_uidiv0(void)
{
	return raise(SIGFPE);
} 

void* slfp_malloc(unsigned int size)
{
	return malloc(size);
}

void slfp_free(void* mem)
{
	free(mem);
}

extern unsigned int gpio_reset;

void slfp_HWReset(void)
{
	sl_log_printf(LEVEL_DEBUG, "slfp_HWReset enter gpio_reset = %u", gpio_reset);

	int error = 0;
	error = qsee_tlmm_gpio_id_out(gpio_reset, QSEE_GPIO_LOW);
	if(error != 0){
    sl_log_printf(LEVEL_ERROR,"qsee_tlmm_gpio_id_out gpio reset failed %d", error);
	}
	slfp_Delay(1);
	
	error = qsee_tlmm_gpio_id_out(gpio_reset, QSEE_GPIO_HIGH);
	if(error != 0){
    sl_log_printf(LEVEL_ERROR,"qsee_tlmm_gpio_id_out gpio reset failed %d", error);
	}
	slfp_Delay(1);	
}

void slfp_HWShutdown(void)
{
	sl_log_printf(LEVEL_DEBUG, "slfp_HWShutdown enter");
	int error = 0;
	error = qsee_tlmm_gpio_id_out(gpio_reset, QSEE_GPIO_LOW);
	if(error != 0){
    sl_log_printf(LEVEL_ERROR,"qsee_tlmm_gpio_id_out gpio reset failed %d", error);
	}
	slfp_Delay(1);
}


int fp_spi_init(void)
{
	#if 0

	int retval = 0;

	

	sl_wrap_gettimeofday(&begin_time, NULL);

	

	retval = qsee_spi_open(FP_SPI_DEVICE_ID);

	

	sl_wrap_gettimeofday(&end_time, NULL);

	sl_log_printf(LEVEL_ERROR, "SPI fp_spi_init time = %d ms %d us", (end_time.tv_sec-begin_time.tv_sec)*1000, (end_time.tv_usec-begin_time.tv_usec));

	

	if(retval != 0)

	{

		silead_qsee_log("qsee_spi_open failed");

	}
	else
	{
	//	silead_qsee_log("qsee_spi_open ok");
	}
	return retval;
	#endif
	return 0;
}

int fp_spi_exit(void)
{
	#if 0
	int retval = 0;
	
	sl_wrap_gettimeofday(&begin_time, NULL);
	
	retval = qsee_spi_close(FP_SPI_DEVICE_ID);
	

	sl_wrap_gettimeofday(&end_time, NULL);
	sl_log_printf(LEVEL_ERROR, "SPI fp_spi_exit time = %d ms %d us", (end_time.tv_sec-begin_time.tv_sec)*1000, (end_time.tv_usec-begin_time.tv_usec));
	
	if(retval != 0)
	{
		silead_qsee_log("qsee_spi_exit failed");
	}
	else
	{
	//	silead_qsee_log("qsee_spi_exit ok");
	}
	return retval;
	#endif
	return 0;
}

int GslSpiSend(const void *tx_buf, void	*rx_buf, int len, unsigned int flag,int spiDMAWrite_enable)
{
  int retval = 0;
  unsigned   char*  ptr = (unsigned  char *)tx_buf;
  unsigned   char  sendFlag = *(ptr +1); 
  if(sendFlag != 0xff && len > 32)
  {
	  unsigned char  reg_buf[4] = {0};
	  retval = slfp_reg_init(0);
	  
	  if(retval < 0){
		  return retval;
	  }
	  
	  g_read_info.buf_addr = (unsigned char *)rx_buf;
	  g_read_info.buf_len = len + 3;
	  g_write_info.buf_addr = reg_buf;
	  g_write_info.buf_len = 2;
  }
  else{     
	   g_write_info.buf_addr = (unsigned char *)tx_buf;
	   g_write_info.buf_len = len;
		
	   g_read_info.buf_addr = (unsigned char *)rx_buf;
	   g_read_info.buf_len =  len;
  }
   retval = qsee_spi_full_duplex(FP_SPI_DEVICE_ID,&g_spi_config,&g_write_info, &g_read_info);
   if(retval!=0)
   {
	       fp_spi_exit();
	  	   return -1;
    }

	return retval;
}

#else //!huawei

int GslSpiSend(const void *tx_buf, void	*rx_buf, int len, unsigned int flag,int spiDMAWrite_enable)
{
  int retval = 0;
     
   g_write_info.buf_addr = (unsigned char *)tx_buf;
   g_write_info.buf_len = len;
	
   g_read_info.buf_addr = (unsigned char *)rx_buf;
   g_read_info.buf_len =  len;
   retval = qsee_spi_full_duplex(FP_SPI_DEVICE_ID,&g_spi_config,&g_write_info, &g_read_info);
   if(retval!=0)
   {
	     //  fp_spi_exit();
	  	   return -1;
    }

	return retval;
}

void slfp_HWReset(void)
{
}

void slfp_HWShutdown(void)
{
}

void* slfp_malloc(unsigned int size)
{
    return qsee_malloc(size);
}

void slfp_free(void* mem)
{
	qsee_free(mem);
}

int __aeabi_idiv0(void)
{
	return raise(SIGFPE);
}

int __aeabi_uidiv0(void)
{
	return raise(SIGFPE);
} 

#endif


#ifdef __cplusplus
}
#endif


