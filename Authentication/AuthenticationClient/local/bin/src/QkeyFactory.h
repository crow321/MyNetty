/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* QkeyFactory.h                                    */
/*                                                      */
/* Copyright (C) QTEC Inc.                              */
/* All rights reserved                                  */
/*                                                      */
/* Author                                               */
/*    pengwb (pengwb@qtec.cn)                           */
/*                                                      */
/* History                                              */
/*    2017/10/18  Create                                */
/*                                                      */
/*------------------------------------------------------*/
#ifndef Q_KEY_FACTORY_H
#define Q_KEY_FACTORY_H
#ifdef __cplusplus
extern "C" {
#endif
/***********************************************
***�������ƣ�InitQuantumKeys
***�������ܣ���ʼ����Կ��Ϣ-����ȫģʽ�¿ɵ���
***��ڲ���������ԿRootkey(48�ֽ�)��������ԿQuantumKey(32�ֽ�)
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int InitQuantumKeys(const unsigned char* Rootkey, const unsigned char *QuantumKey);

/***********************************************
***�������ƣ�Reset
***�������ܣ��ָ�usb-key����̬-����ȫģʽ�¿ɵ���
***��ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int ResetLoad();


/***********************************************
***�������ƣ�GetDeviceCount
***�������ܣ���ȡusb-key����
***��ڲ�����NULL
***���ڲ�����NULL
***����ֵ��0��ʾ��usb�豸
***��    ע��NULL
************************************************/
int GetDeviceCount();
	
/***********************************************
***�������ƣ�GetDeviceInfo
***�������ܣ������豸���кŻ�ȡ�豸��Ϣ
***��ڲ�����
				1,nIndex�豸����
				2,pDeviceId��buffer����pDevLen
***���ڲ�����
				 1,pVersion�豸�汾��
				 2,pDevLen�豸id����
				 3,pDeviceId�豸id
				 4,pVendorId��Ӧ��id
				 5,pProductId��Ʒid
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GetDeviceInfo(int nIndex, unsigned short *pVersion, unsigned short *pDevLen, 
			unsigned char *pDeviceId, unsigned short *pVendorId, unsigned short *pProductId);


/***********************************************
***�������ƣ�InitQuantumKeysByIndex
***�������ܣ������豸���кų�ʼ����Կ
***��ڲ���������ԿRootkey(48�ֽ�)��������ԿQuantumKey(32�ֽ�)�����к�nIndex
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int InitQuantumKeysByIndex(const int nIndex, const unsigned char* Rootkey, const unsigned char *QuantumKey);


/***********************************************
***�������ƣ�DecryptQuantumKeysByIndex
***�������ܣ������豸���кŽ�����Կ
***��ڲ�����
				1,������ϢInEncryptedData
				2,������Ϣ����InEncryptedDatalen
				3,DecryptedKeyInfo��buffer����DecryptedKeyInfoLen
				4,nIndexʹ�õڼ����豸���н���
***���ڲ�����
				1,���ܺ�������ϢDecryptedKeyInfo(key_id+key_value,key_id+key_value...)
				2,���ܺ�������Ϣ����DecryptedKeyInfoLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int DecryptQuantumKeysByIndex(const int nIndex, const unsigned char* InEncryptedData, 
				const unsigned int InEncryptedDatalen, unsigned char* DecryptedKeyInfo, unsigned int *DecryptedKeyInfoLen);

/***********************************************
***�������ƣ�RestartByIndex
***�������ܣ������豸���к������豸
***��ڲ�����NULL
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int RestartByIndex(int nIndex);


/***********************************************
***�������ƣ�ResetPINByIndex
***�������ܣ������豸���к�����PIN��
***��ڲ�����NULL
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int ResetPINByIndex(int nIndex);


#ifdef __cplusplus
}
#endif

#endif
