/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* Qkey.h                                    */
/*                                                      */
/* Copyright (C) QTEC Inc.                              */
/* All rights reserved                                  */
/*                                                      */
/* Author                                               */
/*    pengwb (pengwb@qtec.cn)                           */
/*                                                      */
/* History                                              */
/*    2017/10/08  Create                                */
/*                                                      */
/*------------------------------------------------------*/
#ifndef Q_KEY_H
#define Q_KEY_H
#ifdef __cplusplus
extern "C" {
#endif
/***********************************************
***�������ƣ�GenStartInfo
***�������ܣ���������������֤��Ϣ
***��ڲ�����
				1,OutDevideId��buffer����OutDeviceIdLen
				2,OutStartAuthInfod��buffer����OutStartAuthLen
***���ڲ�����
				1,�豸��־OutDevideId
				2,�豸��־����OutDeviceIdLen
				3,������֤��ϢOutStartAuthInfo
				4,������֤��Ϣ����OutStartAuthLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GenStartInfo(unsigned char *OutDevideId, unsigned char *OutDeviceIdLen, 
				unsigned char *OutStartAuthInfo, unsigned char *OutStartAuthLen);

/***********************************************
***�������ƣ�GenAuthInfo
***�������ܣ����ɽ�����֤��Ϣ
***��ڲ�����
				1,��ս��ϢInChallengeInfo
				2,��ս��Ϣ����InChallengeLen
				3,OutReqAuthInfo��buffer����OutReqAuthLen
***���ڲ�����
				1,������֤��ϢOutReqAuthInfo
				2,������֤��Ϣ����OutReqAuthLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int GenAuthInfo(const unsigned char * InChallengeInfo, const unsigned char InChallengeLen, 
				unsigned char *OutReqAuthInfo, unsigned char *OutReqAuthLen);

/***********************************************
***�������ƣ�CheckAuthInfo
***�������ܣ�У�����������֤��Ϣ
***��ڲ�����
				1,�������֤��ϢInAuthInfo
				2,�������֤��Ϣ����InAuthInfoLen
				3,OutSessionKey��buffer����OutSessionKeyLen
***���ڲ�����
				1,�Ự��Կ��־OutSessionId��16�ֽ�
				2,�Ự��ԿOutSessionKey
				3,�Ự��Կ����OutSessionKeyLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int CheckAuthInfo(const unsigned char* InAuthInfo, const unsigned char InAuthInfoLen, 
				unsigned char* OutSessionId, unsigned char *OutSessionKey, unsigned char *OutSessionKeyLen);

/***********************************************
***�������ƣ�EncryptQuantumKeys
***�������ܣ�ʹ�ø���Կ��������
***��ڲ�����
				1,������ϢInputData
				2,������Ϣ����InputDataLen
				3,EncryptedData��buffer����EncryptedDataLen
***���ڲ�����
				1,���ܺ�������ϢEncryptedData
				2,���ܺ�������Ϣ����EncryptedDataLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int EncryptQuantumKeys(const unsigned char* InputData, const unsigned int InputDataLen, 
				unsigned char* EncryptedData, unsigned int *EncryptedDataLen);

/***********************************************
***�������ƣ�DecryptQuantumKeys
***�������ܣ�ʹ�ø���Կ����Ӧ����Կ
***��ڲ�����
				1,������ϢInEncryptedData
				2,������Ϣ����InEncryptedDatalen
				3,DecryptedKeyInfo��buffer����DecryptedKeyInfoLen
***���ڲ�����
				1,���ܺ�������ϢDecryptedKeyInfo(key_id+key_value,key_id+key_value...)
				2,���ܺ�������Ϣ����DecryptedKeyInfoLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int DecryptQuantumKeys(const unsigned char* InEncryptedData, const unsigned int InEncryptedDatalen, 
				unsigned char* DecryptedKeyInfo, unsigned int *DecryptedKeyInfoLen);

/***********************************************
***�������ƣ�UpdateRootKey
***�������ܣ����¸���Կ
***��ڲ�����
				1,RootKey��buffer����RootKeylen
***���ڲ�����
				1,���¸���Կ��ϢRootKey
				2,���¸���Կ��Ϣ����RootKeylen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int UpdateRootKey(unsigned char* RootKey, unsigned char *RootKeylen);

/***********************************************
***�������ƣ�ConfirmRootKey
***�������ܣ�ȷ�ϸ��¸���Կ
***��ڲ�����
				1,ȷ�ϸ��¸���Կ��ϢRootKey
				2,ȷ�ϸ��¸���Կ��Ϣ����RootKeyLen
***����ֵ��0��ʾ�ɹ�
***��    ע��NULL
************************************************/
int ConfirmRootKey(const unsigned char* RootKey, const unsigned char RootKeyLen);
#ifdef __cplusplus
}
#endif

#endif
