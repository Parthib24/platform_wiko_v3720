/**
 * created by silead
 */
#ifndef __SILEADHARDWAREDETECT_H__
#define __SILEADHARDWAREDETECT_H__


#ifdef __cplusplus
extern "C" {
#endif

int silead_read_chipid(unsigned int *pValue);

int check_silead_sensor(void) ;

bool is_64bit_system(void);

int checkFpSensor(void);

#ifdef __cplusplus
}
#endif

#endif
