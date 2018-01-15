/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* QkeyFile.h                                    */
/*                                                      */
/* Copyright (C) QTEC Inc.                              */
/* All rights reserved                                  */
/*                                                      */
/* Author                                               */
/*    pengwb (pengwb@qtec.cn)                           */
/*                                                      */
/* History                                              */
/*    2017/11/09  Create                                */
/*                                                      */
/*------------------------------------------------------*/
#ifndef Q_KEY_FILE_H
#define Q_KEY_FILE_H
#ifdef __cplusplus
extern "C" {
#endif
/***********************************************
***�������ƣ�LoginShock
***�������ܣ���¼�������
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
 				4.InRandom����������(32Bits)
 ***���ڲ�����
 				1.OutRandom����������(32Bits)
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int LoginShock(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
					unsigned char *InRandom, unsigned char *OutRandom);

/***********************************************
***�������ƣ�LoginResponse
***�������ܣ���¼�������
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
 				4.InLoginResp����ĳ������(32Bits)
 ***���ڲ�����
 				1.OutLoginToken����ĳ��������Token����48Bits
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int LoginResponse(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						 unsigned char *InLoginResp,  unsigned char *OutLoginToken);

/***********************************************
***�������ƣ�ModifyPinCode
***�������ܣ��޸�PIN��
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
				5.InOldPin�ɵ�PIN��(32Bits)
				6.InNewPin�µ�PIN��(32Bits)
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int ModifyPinCode(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InOldPin, unsigned char *InNewPin);

/***********************************************
***�������ƣ�SetUserName
***�������ܣ������û���
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
				5.InUserNameLen�û�������(����С�ڵ���31Bits)
				6.InUserName�û���
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int SetUserName(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char InUserNameLen, unsigned char *InUserName);


/***********************************************
***�������ƣ�GetUserName
***�������ܣ���ȡ�û�����
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.OutUserName��buffer����OutUserNameLen
***���ڲ�����
				1.OutUserNameLen����û�������(С�ڵ���31Bits)
				2.OutUserName�û�����
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GetUserName(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *OutUserNameLen, unsigned char *OutUserName);

/***********************************************
***�������ƣ�UpdateFileKeys
***�������ܣ������ļ�������Կ
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
				5.InKeyCount������Կ����(���2����Կ)
				6.InKeyInfo�������Կ��Ϣ(����ΪInKeyCount x 48)
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int UpdateFileKeys(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char InKeyCount, unsigned char *InKeyInfo);

/***********************************************
***�������ƣ�GetFileKeys
***�������ܣ���ȡ�ļ�������Կ
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
***���ڲ�����
				1.OutKeyId��ȡ���ļ�������ԿID (16Bits)
				2.OutKey��ȡ���ļ�������Կ(32Bits)
				3.OutEncKey����Կ���ܵ���Կ��Ϣ(64Bits)
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GetFileKeys(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *OutKeyId, 
						unsigned char *OutKey, unsigned char *OutEncKey);

/***********************************************
***�������ƣ�GetFileKeysAck
***�������ܣ���ȡ�ļ�������Կ����
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
				5.InKeyId�������ԿID(16Bits)
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GetFileKeysAck(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InKeyId);

/***********************************************
***�������ƣ�GetFileKeysNumber
***�������ܣ���ȡ�ļ�������Կ����
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
***���ڲ�����
				1.OutFileKeyCount�������Կ����
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GetFileKeysNumber(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned short *OutFileKeyCount);

/***********************************************
***�������ƣ�DecryptFileKey
***�������ܣ����ܸ���Կ���ܵ���Կ
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
				5.InEncKey����ļ�����Կ(64Bits)
***���ڲ�����
				1.OutKeyId����Ľ�����ԿID(16Bits)
				2.OutKey����Ľ�����Կ(32Bits)
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int DecryptFileKey(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InEncKey,
						unsigned char *OutKeyId, unsigned char *OutKey);

/***********************************************
***�������ƣ�DeleteFileKey
***�������ܣ�ɾ����Կ
***��ڲ�����
				1.InIndex�豸���к�(������ڵ��� 0)
				2.InDevideLen�豸id����
				3.InDeviceId�豸id
				4.InLoginToken�����Token����(48Bits)
				5.wCount�����ɾ����Կ����
***���ڲ�����NULL
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int DeleteFileKey(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned short wCount);



#ifdef __cplusplus
}
#endif

#endif
