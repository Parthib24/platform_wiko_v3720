#include <stdint.h>
#include <qsee_spi.h>
#include "fp_log.h"

#define FPSENSOR_SPI_WORD_SIZE_BYTES 8
#define FPSENSOR_MAX_SPI_FREQ	(4 * 1000 * 1000)
extern qsee_spi_device_id_t fpsensor_qsee_spi_id;

static int spi_clk_enabled = 0;

//fpsensor default use QSEE_SPI_DEVICE_6
static qsee_spi_device_id_t spi_dev_id = QSEE_SPI_DEVICE_6;
static qsee_spi_config_t fp_spi_config =
{
    .spi_bits_per_word = FPSENSOR_SPI_WORD_SIZE_BYTES,
    .spi_clk_always_on = QSEE_SPI_CLK_NORMAL,
    .spi_clk_polarity = QSEE_SPI_CLK_IDLE_LOW,
    .spi_cs_mode = QSEE_SPI_CS_KEEP_ASSERTED,
    .spi_cs_polarity = QSEE_SPI_CS_ACTIVE_LOW,
    .spi_shift_mode = QSEE_SPI_INPUT_FIRST_MODE,
	.max_freq = FPSENSOR_MAX_SPI_FREQ,
	.hs_mode = 1,
	.loopback = 0,
	.spi_slave_index = 0,
	.deassertion_time_ns = 0,
};

int fpsensor_spi_init(int freq_low_khz, int freq_high_khz)
{
    int retval = 0;

    spi_dev_id = fpsensor_qsee_spi_id;
	LOGD("%s init spi port %d\n",__func__, spi_dev_id);
    retval = qsee_spi_open(spi_dev_id);
    if (retval != 0) {
        LOGE("%s: qsee_spi_open FAILED on device: %d with retval = %d", __func__, spi_dev_id, retval);
		return retval;
    }
    spi_clk_enabled = 1;
	LOGD("%s init spi port %d ok, clk freq:%dhz\n", __func__, spi_dev_id, FPSENSOR_MAX_SPI_FREQ);
    return retval;
}

int fpsensor_spi_clk_disable(void);
static int fpsensor_spi_writeread(uint8_t *tx, size_t tx_bytes, uint8_t *rx, size_t rx_bytes)
{
    int retval = 0;
    int retry = 0;

    qsee_spi_transaction_info_t w_info = {
        .buf_addr    = tx,
        .buf_len     = tx_bytes,
        .total_bytes = 0,
    };
    qsee_spi_transaction_info_t r_info = {
        .buf_addr    = rx,
        .buf_len     = rx_bytes,
        .total_bytes = 0,
    };

spi_retry:
    // check and enable spi
    if (spi_clk_enabled == 0) {
        retval = qsee_spi_open(spi_dev_id);
        if (retval != 0) {
            LOGE("%s: qsee_spi_open FAILED on device: %d with retval = %d", __func__, spi_dev_id, retval);
		    return retval;
        }
        spi_clk_enabled = 1;
	    LOGD("%s enable spi ok\n", __func__);
    }
    retval = qsee_spi_full_duplex(spi_dev_id, &fp_spi_config, &w_info, &r_info);
    if (retval != 0) {
        LOGE("%s - qsee_spi_full_duplex FAILED: retval = %d", __func__, retval);
    }

    if (retval == -1) {
        LOGE("%s re-enable spi clock\n", __func__);
        fpsensor_spi_clk_disable();
        if (retry++ < 3)
            goto spi_retry;
        LOGE("%s re-enable spi clock timeout, check your hardware!!!!\n", __func__);
    }

    return retval;
}

int fpsensor_spi_writeread_fifo(char *tx, char *rx, int tx_len, int send_len)
{
    return fpsensor_spi_writeread((uint8_t *)tx, tx_len, (uint8_t *)rx, send_len);
}

int fpsensor_spi_writeread_dma(char *tx, char *rx, int send_len)
{
    return fpsensor_spi_writeread((uint8_t *)tx, send_len, (uint8_t *)rx, send_len);
}

int fpsensor_spi_clk_disable(void)
{
    int ret = 0;

    if (spi_clk_enabled == 0) {
        LOGD("%s: spi already closed!", __func__);
        return ret;
    }

    ret = qsee_spi_close(spi_dev_id);
    if (ret) {
        LOGE("%s: close spi error!", __func__);
        return ret;
    } else {
        LOGD("%s: close spi ok!", __func__);
        spi_clk_enabled = 0;
        return ret;
    }
}
