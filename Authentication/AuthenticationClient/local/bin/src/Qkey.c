#include "Qkey.h"
#include "QkeyFactory.h"
#include "HidTrans.h"

/*Declare*/
int ProcessTwoInTwoOut(const int nIndex, const EQkFunCode eFunCode, 
				const unsigned char * InData, const unsigned int InDataLen, 
				unsigned char *OutData, unsigned int *OutDataLen);

int InitQuantumKeys(const unsigned char* Rootkey, const unsigned char *QuantumKey)
{
	return InitQuantumKeysByIndex(0, Rootkey, QuantumKey);
}


int InitQuantumKeysByIndex(const int nIndex, const unsigned char* Rootkey, const unsigned char *QuantumKey)
{
	int nOffset = 0;
	UINT nRecLen = 0;
	int nRet = QK_SUCCESS;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	if(NULL == Rootkey || NULL == QuantumKey || nIndex > MAX_DEVICE_COUNT){
		QKEY_DEBUG("InitQuantumKeys argument error!");
		return QK_ERROR_ARG;
	}

	/*Set root key len*/
	nRet = FcLittleEndianAddShortToData(szSendData, DEFAULT_KEY_ID_LEN + DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("InitQuantumKeys add root key len error!");
		return QK_ERROR_PACK;
	}
	nOffset += 2;

	/*Set root key*/
	memcpy(szSendData + nOffset, Rootkey, DEFAULT_KEY_ID_LEN + DEFAULT_KEY_LEN);
	nOffset += (DEFAULT_KEY_ID_LEN + DEFAULT_KEY_LEN);
	
	/*Set quantum key len*/
	nRet = FcLittleEndianAddShortToData(szSendData + nOffset, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("InitQuantumKeys add root key len error!");
		return QK_ERROR_PACK;
	}
	nOffset += 2;

	/*Set quantum key*/
	memcpy(szSendData + nOffset, QuantumKey, DEFAULT_KEY_LEN);
	nOffset += DEFAULT_KEY_LEN;

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_INIT_QUANTUMKEYS, nIndex, szSendData, nOffset, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("InitQuantumKeys:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}

int GenStartInfo(unsigned char *OutDevideId, unsigned char *OutDeviceIdLen, 
				unsigned char *OutStartAuthInfo, unsigned char *OutStartAuthLen)
{
	int nOffset = 0, nTmpLen = 0;
	int nRet = QK_SUCCESS;
	UINT nRecLen = MAX_BUFF_LEN;
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
	if(NULL == OutDevideId || 0 == *OutDeviceIdLen
		|| NULL == OutStartAuthInfo || 0 == *OutStartAuthLen){
		QKEY_DEBUG("GenStartInfo argument error!");
		return QK_ERROR_ARG;
	}

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_GEN_STARTINFO, 0, NULL, 0, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GenStartInfo:HidTransSendRec error!");
		return nRet;
	}
	
	/*Get OutDeviceIdLen*/
	nTmpLen = GETSHORT(szRecvData);
	if(nTmpLen > *OutDeviceIdLen){
		QKEY_DEBUG("GenStartInfo get device id len error, buffer len is less than value!"
			"nTmpLen = %d, *OutDeviceIdLen = %d", nTmpLen, *OutDeviceIdLen);
		return QK_ERROR_ARG;
	}
	memset(OutDevideId, 0, *OutDeviceIdLen);
	*OutDeviceIdLen = nTmpLen;
	nOffset += 2;

	/*Get OutDevideId*/
	memcpy(OutDevideId, szRecvData + nOffset, *OutDeviceIdLen);
	nOffset += *OutDeviceIdLen;
	
	/*Get OutStartAuthLen*/
	nTmpLen = GETSHORT(szRecvData + nOffset);
	if(nTmpLen > *OutStartAuthLen){
		QKEY_DEBUG("GenStartInfo get start auth len error, buffer len is less than value!"
			" *OutStartAuthLen:%d, nTmpLen:%d", *OutStartAuthLen, nTmpLen);
		return QK_ERROR_ARG;
	}
	memset(OutStartAuthInfo, 0, *OutStartAuthLen);
	*OutStartAuthLen = nTmpLen;
	nOffset += 2;

	/*Get OutStartAuthInfo*/
	memcpy(OutStartAuthInfo, szRecvData + nOffset, *OutStartAuthLen);
	
	return QK_SUCCESS;
}

int GenAuthInfo(const unsigned char * InChallengeInfo, const unsigned char InChallengeLen, 
				unsigned char *OutReqAuthInfo, unsigned char *OutReqAuthLen)
{
	int nRet = 0;
	unsigned int nOutLen = *OutReqAuthLen;
	unsigned int nInLen = InChallengeLen;
	if(NULL == InChallengeInfo || 0 == InChallengeLen
		|| NULL == OutReqAuthInfo || 0 == *OutReqAuthLen){
		QKEY_DEBUG("GenStartInfo argument error!");
		return QK_ERROR_ARG;
	}

	nRet = ProcessTwoInTwoOut(0, FC_GEN_AUTHINFO, InChallengeInfo, nInLen,
							OutReqAuthInfo, &nOutLen);
	if(QK_SUCCESS != nRet)
	{
		QKEY_DEBUG("GenAuthInfo:ProcessTwoInTwoOut error!");
		return nRet;
	}

	*OutReqAuthLen = nOutLen;
	
	return QK_SUCCESS;
}

int CheckAuthInfo(const unsigned char* InAuthInfo, const unsigned char InAuthInfoLen, 
				unsigned char* OutSessionId, unsigned char *OutSessionKey, unsigned char *OutSessionKeyLen)
{
	int nOffset = 0,nTmpLen = 0;
	UINT nRecLen = 0;
	int nRet = QK_SUCCESS;
	WORD nSessionIdLen = 0;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};

	if(NULL == InAuthInfo || 0 == InAuthInfoLen
		|| NULL == OutSessionId || NULL == OutSessionKey 
		|| 0 == *OutSessionKeyLen){
		QKEY_DEBUG("CheckAuthInfo argument error!");
		return QK_ERROR_ARG;
	}

	
	/*Set InAuthInfoLen len*/
	nRet = FcLittleEndianAddShortToData(szSendData, InAuthInfoLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("CheckAuthInfo add challenge len error!");
		return QK_ERROR_PACK;
	}
	nOffset += 2;

	/*Set challenge*/
	memcpy(szSendData + nOffset, InAuthInfo, InAuthInfoLen);
	nOffset += InAuthInfoLen;

	/*Send to u-key process*/
	nRecLen = MAX_BUFF_LEN;
	nRet = HidTransSendRec(FC_CHECK_AUTHINFO, 0, szSendData, nOffset, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("CheckAuthInfo:HidTransSendRec error!");
		return nRet;
	}
	nOffset = 0;
	
	/*Get OutSessionIdLen*/
	nSessionIdLen = GETSHORT(szRecvData);
	nOffset += 2;
	
	/*Get OutSessionId*/
	memset(OutSessionId, 0, DEFAULT_KEY_ID_LEN);
	memcpy(OutSessionId, szRecvData + nOffset, DEFAULT_KEY_ID_LEN);
	nOffset += DEFAULT_KEY_ID_LEN;

	/*Get OutSessionKeyLen*/
	nTmpLen = GETSHORT(szRecvData + nOffset);
	if(nTmpLen > *OutSessionKeyLen){
		QKEY_DEBUG("CheckAuthInfo get OutReqAuthLen error, buffer len is less than value!"
			" *OutSessionKeyLen:%d, nTmpLen:%d", *OutSessionKeyLen, nTmpLen);
		return QK_ERROR_ARG;
	}
	memset(OutSessionKey, 0, *OutSessionKeyLen);
	*OutSessionKeyLen = nTmpLen;
	nOffset += 2;

	/*Get OutSessionKey*/
	memcpy(OutSessionKey, szRecvData + nOffset, *OutSessionKeyLen);
	
	return QK_SUCCESS;
}

int EncryptQuantumKeys(const unsigned char* InputData, const unsigned int InputDataLen, 
				unsigned char* EncryptedData, unsigned int *EncryptedDataLen)
{
	int nRet = QK_SUCCESS;
	if(NULL == InputData || 0 == InputDataLen
		|| NULL == EncryptedData || 0 == *EncryptedDataLen){
		QKEY_DEBUG("EncryptQuantumKeys argument error!");
		return QK_ERROR_ARG;
	}

	nRet = ProcessTwoInTwoOut(0, FC_ENCRYPT_QUANTUMKEYS, InputData, InputDataLen, 
							EncryptedData, EncryptedDataLen);
	if(QK_SUCCESS != nRet)
	{
		QKEY_DEBUG("EncryptQuantumKeys:ProcessTwoInTwoOut error!");
		return nRet;
	}
	return QK_SUCCESS;
}

int DecryptQuantumKeys(const unsigned char* InEncryptedData, const unsigned int InEncryptedDatalen, 
				unsigned char* DecryptedKeyInfo, unsigned int *DecryptedKeyInfoLen)
{
	return DecryptQuantumKeysByIndex(0, InEncryptedData, InEncryptedDatalen, DecryptedKeyInfo, DecryptedKeyInfoLen);
}

int DecryptQuantumKeysByIndex(const int nIndex, const unsigned char* InEncryptedData, 
				const unsigned int InEncryptedDatalen, unsigned char* DecryptedKeyInfo, unsigned int *DecryptedKeyInfoLen)
{
	int nRet = QK_SUCCESS;
	if(NULL == InEncryptedData || 0 == InEncryptedDatalen
		|| NULL == DecryptedKeyInfo || 0 == *DecryptedKeyInfoLen
		|| nIndex > MAX_DEVICE_COUNT){
		QKEY_DEBUG("DecryptQuantumKeys argument error!");
		return QK_ERROR_ARG;
	}

	nRet = ProcessTwoInTwoOut(nIndex, FC_DECRYPT_QUANTUMKEYS, InEncryptedData, InEncryptedDatalen, 
							DecryptedKeyInfo, DecryptedKeyInfoLen);
	if(QK_SUCCESS != nRet)
	{
		QKEY_DEBUG("DecryptQuantumKeys:ProcessTwoInTwoOut error!");
		return nRet;
	}
	return QK_SUCCESS;
}


int UpdateRootKey(unsigned char* RootKey, unsigned char *RootKeylen)
{	
	int nRet = QK_SUCCESS;
	int nOffset = 0,nTmpLen = 0;
	UINT nRecLen = MAX_BUFF_LEN;
	BYTE szRecvData[MAX_BUFF_LEN] = {0};

	if(NULL == RootKey || 0 == *RootKeylen){
		QKEY_DEBUG("UpdateRootKey argument error!");
		return QK_ERROR_ARG;
	}

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_UPDATE_ROOTKEY, 0, NULL, 0, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("UpdateRootKey:HidTransSendRec error!");
		return nRet;
	}
	
	/*Get RootKeylen*/
	nTmpLen = GETSHORT(szRecvData);
	if(nTmpLen > *RootKeylen){
		QKEY_DEBUG("UpdateRootKey get device id len error, buffer len is less than value!"
			" *RootKeylen:%d, nTmpLen:%d", *RootKeylen, nTmpLen);
		return QK_ERROR_ARG;
	}
	memset(RootKey, 0, *RootKeylen);
	*RootKeylen = nTmpLen;
	nOffset += 2;

	/*Get RootKey*/
	memcpy(RootKey, szRecvData + nOffset, *RootKeylen);

	return QK_SUCCESS;
}

int ConfirmRootKey(const unsigned char* RootKey, const unsigned char RootKeyLen)
{
	int nRet = QK_SUCCESS;
	int nOffset = 0;
	UINT nRecLen = 0;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};

	if(NULL == RootKey || 0 == RootKeyLen){
		QKEY_DEBUG("ConfirmRootKey argument error!");
		return QK_ERROR_ARG;
	}

	/*Set root key len*/
	nRet = FcLittleEndianAddShortToData(szSendData, RootKeyLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ConfirmRootKey add root key len error!");
		return QK_ERROR_PACK;
	}
	nOffset += 2;

	/*Set root key*/
	memcpy(szSendData + nOffset, RootKey, RootKeyLen);
	nOffset += RootKeyLen;

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_CONFIRM_ROOTKEY, 0, szSendData, nOffset, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ConfirmRootKey:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}


int ProcessTwoInTwoOut(const int nIndex, const EQkFunCode eFunCode, 
				const unsigned char * InData, const unsigned int InDataLen, 
				unsigned char *OutData, unsigned int *OutDataLen)
{
	int nRet = QK_SUCCESS;
	UINT nRecLen = 0;
	int nOffset = 0, nTmpLen = 0;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
	
	/*Set InDataLen*/
	nRet = FcLittleEndianAddShortToData(szSendData, InDataLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ProcessTwoInTwoOut add data len error!");
		return QK_ERROR_PACK;
	}
	nOffset += 2;

	/*Set InData*/
	memcpy(szSendData + nOffset, InData, InDataLen);
	nOffset += InDataLen;

	/*Send to u-key process*/
	nRecLen = MAX_BUFF_LEN;
	nRet = HidTransSendRec(eFunCode, nIndex, szSendData, nOffset, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ProcessTwoInTwoOut:HidTransSendRec error!");
		return nRet;
	}
	nOffset = 0;
	
	/*Get OutDataLen*/
	nTmpLen = GETSHORT(szRecvData);
	if((UINT)nTmpLen > *OutDataLen){
		QKEY_DEBUG("ProcessTwoInTwoOut get OutDataLen error, buffer len is less than value!"
			"get len:%d, recv len:%d", nTmpLen, *OutDataLen);
		return QK_ERROR_ARG;
	}
	memset(OutData, 0, *OutDataLen);
	*OutDataLen = nTmpLen;
	nOffset += 2;

	/*Get OutData*/
	memcpy(OutData, szRecvData + nOffset, *OutDataLen);
	
	return QK_SUCCESS;
}

int ResetLoad()
{
	UINT nRecLen = 0;
	int nRet = QK_SUCCESS;

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_RESET_LOAD, 0, NULL, 0, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("Reset:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}

int GetDeviceCount()
{
	return HidTransGetCount();
}

int GetDeviceInfo(int nIndex, unsigned short *pVersion, unsigned short *pDevLen, 
			unsigned char *pDeviceId, unsigned short *pVendorId, unsigned short *pProductId)
{
	UINT nRecLen = 0;
	int nRet = QK_SUCCESS;
	int nOffset = 0, nTmpLen = 0;
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
	
	/*Send to u-key process*/
	nRecLen = MAX_BUFF_LEN;
	nRet = HidTransSendRec(FC_GET_DEVICE_INFO, nIndex, NULL, 0, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ProcessTwoInTwoOut:HidTransSendRec error!");
		return nRet;
	}

	/*Get version*/
	*pVersion = GETSHORT(szRecvData);
	nOffset += 2;

	/*Get device id len*/
	nTmpLen = GETSHORT(szRecvData + nOffset);
	if(nTmpLen > *pDevLen){
		QKEY_DEBUG("GetDeviceInfo get pDevLen error, buffer len is less than value!"
			"get len:%d, recv len:%d", nTmpLen, *pDevLen);
		return QK_ERROR_ARG;
	}
	memset(pDeviceId, 0, *pDevLen);
	*pDevLen = nTmpLen;
	nOffset += 2;

	/*Get device id*/
	memcpy(pDeviceId, szRecvData + nOffset, *pDevLen);
	nOffset += *pDevLen;

	/*Get vendor id*/
	*pVendorId = GETSHORT(szRecvData + nOffset);
	nOffset += 2;

	/*Get product id*/
	*pProductId = GETSHORT(szRecvData + nOffset);

	return QK_SUCCESS;
}


int RestartByIndex(int nIndex)
{
	UINT nRecLen = 0;
	int nRet = QK_SUCCESS;

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_RESTART, nIndex, NULL, 0, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("Reset:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}

int ResetPINByIndex(int nIndex)
{
	UINT nRecLen = 0;
	int nRet = QK_SUCCESS;

	/*Send to u-key process*/
	nRet = HidTransSendRec(FC_RESET_PIN, nIndex, NULL, 0, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("Reset:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}



