#ifndef __GSL_TBASE_DRIVER_WAPPER__
#define __GSL_TBASE_DRIVER_WAPPER__

typedef struct {
	int 	Chip_select;
	int 	Bits_per_word;
	int 	Pages;
    int 	Offset;
    int 	Speed;
	int		High_time;
 	int 	Low_time;
    int 	Reset_gpio;
    int 	Reset;
    int 	Mode;    /*useless*/
	int 	Com_mod;
    int 	DMAWrite_enable;
    int 	ioctllength;
}__attribute__ ((packed)) spi_conf_t ;

int GslSpiConfig(spi_conf_t *spi_config);
int GslSpiSend(const void *tx_buf, void	*rx_buf, int len, unsigned int flag,int spiSingleWrite_TZ_Enbale);
void slfp_Delay(int msecs);

#endif


