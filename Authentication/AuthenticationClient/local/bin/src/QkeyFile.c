#include "crypto.h"
#include "HidTrans.h"
#include "QkeyFile.h"

#pragma pack(1)
typedef struct MsgHeadTag
{
	BYTE	bVersion;
	BYTE	bEncAlg;
	WORD	wMsgLen;
	BYTE	szKeyId[DEFAULT_KEY_ID_LEN];
} MsgHeadT;

typedef struct DevIdTag
{
	BYTE	bDevIdLen;
	BYTE	szDevId[DEFAULT_DEV_ID_LEN];
}DevIdT;

#pragma pack()

static void InitHead(MsgHeadT *pMsgHead, BYTE bEncAlg, WORD wMsgLen, BYTE *pKeyId)
{
	pMsgHead->bVersion = DEFAULT_FILE_VERSION;
	pMsgHead->bEncAlg = bEncAlg;
	pMsgHead->wMsgLen = wMsgLen;
	FuncSwapShort(&(pMsgHead->wMsgLen));
	if(pKeyId != NULL)
		memcpy(pMsgHead->szKeyId, pKeyId, DEFAULT_KEY_ID_LEN);
}

static int CheckHead(MsgHeadT *pMsgHead)
{
	if(pMsgHead->bVersion != DEFAULT_FILE_VERSION)
	{
		QKEY_DEBUG("CheckHead error!");
		return QK_ERROR_CHECK;
	}

	return QK_SUCCESS;
}

int LoginShock(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
					unsigned char *InRandom, unsigned char *OutRandom)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = MAX_BUFF_LEN, nToLen = 0;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataTag{
		DevIdT sDevId;
		BYTE	szRandom[DEFAULT_RANDOM_LEN];
	}*pDataIn, *pDataOut;
#pragma pack()	
	/*init send data*/
	//init body
	pDataIn = (struct DataTag *)(szSendData + sizeof(MsgHeadT));
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	memcpy(pDataIn->szRandom, InRandom, DEFAULT_RANDOM_LEN);
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_NULL, sizeof(struct DataTag), NULL);

	//send and recv
	nToLen = sizeof(MsgHeadT) + sizeof(struct DataTag);
	nRet = HidTransSendRec(FC_LOGIN_SHOCK, InIndex, szSendData, nToLen, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("LoginShock:HidTransSendRec error!");
		return nRet;
	}

	/*process recv data*/
	pMsgHead = (MsgHeadT *)szRecvData;
	//check head
	nRet = CheckHead(pMsgHead);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("LoginShock:CheckHead error!");
		return nRet;
	}
	FuncSwapShort(&(pMsgHead->wMsgLen));
	if(pMsgHead->wMsgLen != sizeof(struct DataTag)){
		QKEY_DEBUG("LoginShock:check msg length error!");
		return QK_ERROR_CHECK;
	}

	//check body
	pDataOut = (struct DataTag *)(szRecvData + sizeof(MsgHeadT));
	if(pDataOut->sDevId.bDevIdLen != InDevideLen || memcmp(pDataOut->sDevId.szDevId, InDeviceId, InDevideLen) != 0){
		QKEY_DEBUG("LoginShock:CheckDevId error!pDataOut->sDevId.bDevIdLen=%d, InDevideLen=%d", \
			pDataOut->sDevId.bDevIdLen, InDevideLen);
		PrintData((unsigned char *)"pDataOut->sDevId.szDevId", pDataOut->sDevId.szDevId, pDataOut->sDevId.bDevIdLen);
		PrintData((unsigned char *)"InDeviceId", InDeviceId, InDevideLen);
		return QK_ERROR_CHECK;
	}

	memcpy(OutRandom, pDataOut->szRandom, DEFAULT_RANDOM_LEN);
	
	return QK_SUCCESS;
}

 
int LoginResponse(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						 unsigned char *InLoginResp,  unsigned char *OutLoginToken)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = MAX_BUFF_LEN, nToLen = 0;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		BYTE	szLoginResp[DEFAULT_RANDOM_LEN];
	}*pDataIn;
	
	struct DataOutTag{
		DevIdT sDevId;
		BYTE	szToken[DEFAULT_TOKEN_LEN];
	}*pDataOut;
#pragma pack()	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)(szSendData + sizeof(MsgHeadT));
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	memcpy(pDataIn->szLoginResp, InLoginResp, DEFAULT_RANDOM_LEN);
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_NULL, sizeof(struct DataInTag), NULL);

	//send and recv
	nToLen = sizeof(MsgHeadT) + sizeof(struct DataInTag);
	nRet = HidTransSendRec(FC_LOGIN_RESPONSE, InIndex, szSendData, nToLen, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("LoginResponse:HidTransSendRec error!");
		return nRet;
	}

	/*process recv data*/
	pMsgHead = (MsgHeadT *)szRecvData;
	//check head
	nRet = CheckHead(pMsgHead);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("LoginResponse:CheckHead error!");
		return nRet;
	}
	FuncSwapShort(&(pMsgHead->wMsgLen));
	if(pMsgHead->wMsgLen != sizeof(struct DataOutTag)){
		QKEY_DEBUG("LoginResponse:check msg length error!");
		return QK_ERROR_CHECK;
	}

	//check body
	pDataOut = (struct DataOutTag*)(szRecvData + sizeof(MsgHeadT));
	if(pDataOut->sDevId.bDevIdLen != InDevideLen || memcmp(pDataOut->sDevId.szDevId, InDeviceId, InDevideLen) != 0){
		QKEY_DEBUG("LoginResponse:CheckDevId error!");
		return QK_ERROR_CHECK;
	}

	memcpy(OutLoginToken, pDataOut->szToken, DEFAULT_TOKEN_LEN);
	
	return QK_SUCCESS;
}



 
int ModifyPinCode(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InOldPin, unsigned char *InNewPin)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = 0, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		BYTE	szOldPin[DEFAULT_PIN_LEN];
		BYTE	szNewPin[DEFAULT_PIN_LEN];
	}*pDataIn;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	memcpy(pDataIn->szOldPin, InOldPin, DEFAULT_PIN_LEN);
	memcpy(pDataIn->szNewPin, InNewPin, DEFAULT_PIN_LEN);

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ModifyPinCode:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nRecLen = 0;
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_MODIFY_PIN_CODE, InIndex, szSendData, nToLen, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("ModifyPinCode:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}

 
int SetUserName(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char InUserNameLen, unsigned char *InUserName)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = 0, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		BYTE	szUserName[MAX_USER_NAME_LEN];
	}*pDataIn;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	memcpy(pDataIn->szUserName, InUserName, InUserNameLen);

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("SetUserName:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nRecLen = 0;
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_SET_USER_NAME, InIndex, szSendData, nToLen, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("SetUserName:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}


 
int GetUserName(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						 unsigned char *OutUserNameLen, unsigned char *OutUserName)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = MAX_BUFF_LEN, nToLen = 0;
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
	}*pDataIn;
	struct DataOutTag{
		DevIdT sDevId;
		BYTE	szUserName[MAX_USER_NAME_LEN];
	}*pDataOut;
#pragma pack()
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)(szSendData + sizeof(MsgHeadT));
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);

	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_NULL, sizeof(struct DataInTag), NULL);

	//send and recv
	nToLen = sizeof(MsgHeadT) + sizeof(struct DataInTag);
	nRet = HidTransSendRec(FC_GET_USER_NAME, InIndex, szSendData, nToLen, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetUserName:HidTransSendRec error!");
		return nRet;
	}

	/*process recv data*/
	pMsgHead = (MsgHeadT *)szRecvData;
	//check head
	nRet = CheckHead(pMsgHead);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetUserName:CheckHead error!");
		return nRet;
	}
	FuncSwapShort(&(pMsgHead->wMsgLen));
	if(pMsgHead->wMsgLen != sizeof(struct DataOutTag)){
		QKEY_DEBUG("GetUserName:check msg length error!");
		return QK_ERROR_CHECK;
	}

	//check body
	pDataOut = (struct DataOutTag *)(szRecvData + sizeof(MsgHeadT));
	if(pDataOut->sDevId.bDevIdLen != InDevideLen || memcmp(pDataOut->sDevId.szDevId, InDeviceId, InDevideLen) != 0){
		QKEY_DEBUG("GetUserName:CheckDevId error!");
		return QK_ERROR_CHECK;
	}

	*OutUserNameLen = (unsigned char)strlen(pDataOut->szUserName);
	strcpy(OutUserName, pDataOut->szUserName);
	
	return QK_SUCCESS;
}


 
int UpdateFileKeys(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char InKeyCount, unsigned char *InKeyInfo)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = 0, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		WORD	sCount;
		BYTE	szKeyInfo[2*DEFAULT_KEY_INFO_LEN];
	}*pDataIn;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);

	if(InKeyCount > 2 || InKeyCount <= 0)
	{
		QKEY_DEBUG("UpdateFileKeys:argument error!");
		return QK_ERROR_ARG;
	}
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	pDataIn->sCount = InKeyCount;
	memcpy(pDataIn->szKeyInfo, InKeyInfo, InKeyCount*DEFAULT_KEY_INFO_LEN);

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("UpdateFileKeys:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nRecLen = 0;
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_UPDATE_FILE_KEYS, InIndex, szSendData, nToLen, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("UpdateFileKeys:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}


int GetFileKeys(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *OutKeyId, 
						unsigned char *OutKey, unsigned char *OutEncKey)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = MAX_BUFF_LEN, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
	}*pDataIn;
	struct DataOutTag{
		DevIdT sDevId;
		BYTE szKeyId[DEFAULT_KEY_ID_LEN];
		BYTE szKey[DEFAULT_KEY_LEN];
		BYTE szEncKey[2*DEFAULT_KEY_LEN];
	}*pDataOut;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeys:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_GET_FILE_KEYS, InIndex, szSendData, nToLen, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeys:HidTransSendRec error!");
		return nRet;
	}

	//check key_id
	pMsgHead = (MsgHeadT *)szRecvData;
	//check head
	nRet = CheckHead(pMsgHead);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetUserName:CheckHead error!");
		return nRet;
	}
	FuncSwapShort(&(pMsgHead->wMsgLen));

	if(memcmp(pMsgHead->szKeyId, InLoginToken, DEFAULT_KEY_ID_LEN) != 0){
		QKEY_DEBUG("GetFileKeys:check recv key_id error!");
		return QK_ERROR_CHECK;
	}
	
	//decrypt data
	nCryptLen = MAX_BUFF_LEN;
	memset(szPduTmp, 0, MAX_BUFF_LEN);
	nRet = Decrypt(CRYPTO_AES_CBC, szRecvData+sizeof(MsgHeadT), pMsgHead->wMsgLen, szPduTmp, &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeys:Decrypt error!");
		return nRet;
	}

	if(nCryptLen != sizeof(struct DataOutTag)){
		QKEY_DEBUG("GetFileKeys:check recv msg error!");
		return QK_ERROR_CHECK;
	}
	
	pDataOut = (struct DataOutTag*)szPduTmp;
	if(pDataOut->sDevId.bDevIdLen != InDevideLen || memcmp(pDataOut->sDevId.szDevId, InDeviceId, InDevideLen) != 0){
		QKEY_DEBUG("GetFileKeys:CheckDevId error!");
		return QK_ERROR_CHECK;
	}

	memcpy(OutKeyId, pDataOut->szKeyId, DEFAULT_KEY_ID_LEN);
	memcpy(OutKey, pDataOut->szKey, DEFAULT_KEY_LEN);
	memcpy(OutEncKey, pDataOut->szEncKey, 2*DEFAULT_KEY_LEN);
	
	return QK_SUCCESS;
}


 
int GetFileKeysAck(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InKeyId)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = 0, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		BYTE	szKeyId[DEFAULT_KEY_ID_LEN];
	}*pDataIn;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);

	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	memcpy(pDataIn->szKeyId, InKeyId, DEFAULT_KEY_ID_LEN);

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysAck:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nRecLen = 0;
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_GET_FILES_ACK, InIndex, szSendData, nToLen, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysAck:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}


int GetFileKeysNumber(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned short *OutFileKeyCount)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = MAX_BUFF_LEN, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
	}*pDataIn;
	struct DataOutTag{
		DevIdT sDevId;
		WORD 	wCount;
	}*pDataOut;
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);
#pragma pack()	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysNumber:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_GET_FILES_NUMBER, InIndex, szSendData, nToLen, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysNumber:HidTransSendRec error!");
		return nRet;
	}

	//check key_id
	pMsgHead = (MsgHeadT *)szRecvData;
	//check head
	nRet = CheckHead(pMsgHead);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetUserName:CheckHead error!");
		return nRet;
	}
	FuncSwapShort(&(pMsgHead->wMsgLen));

	if(memcmp(pMsgHead->szKeyId, InLoginToken, DEFAULT_KEY_ID_LEN) != 0){
		QKEY_DEBUG("GetFileKeysNumber:check recv key_id error!");
		return QK_ERROR_CHECK;
	}
	
	//decrypt data
	nCryptLen = MAX_BUFF_LEN;
	memset(szPduTmp, 0, MAX_BUFF_LEN);
	nRet = Decrypt(CRYPTO_AES_CBC, szRecvData+sizeof(MsgHeadT), pMsgHead->wMsgLen, szPduTmp, &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysNumber:Decrypt error!");
		return nRet;
	}

	if(nCryptLen != sizeof(struct DataOutTag)){
		QKEY_DEBUG("GetFileKeysNumber:check recv msg error!, nCryptLen = %d, sizeof(struct DataOutTag) = %d", nCryptLen, sizeof(struct DataOutTag));
		return QK_ERROR_CHECK;
	}
	
	pDataOut = (struct DataOutTag*)szPduTmp;
	if(pDataOut->sDevId.bDevIdLen != InDevideLen || memcmp(pDataOut->sDevId.szDevId, InDeviceId, InDevideLen) != 0){
		QKEY_DEBUG("GetFileKeysNumber:CheckDevId error!");
		return QK_ERROR_CHECK;
	}

	*OutFileKeyCount = pDataOut->wCount;
	
	return QK_SUCCESS;
}


 
int DecryptFileKey(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InEncKey,
						unsigned char *OutKeyId, unsigned char *OutKey)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = MAX_BUFF_LEN, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
	BYTE szRecvData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		BYTE	szEncKey[2*DEFAULT_KEY_LEN];
	}*pDataIn;
	struct DataOutTag{
		DevIdT sDevId;
		BYTE	szKeyId[DEFAULT_KEY_ID_LEN];
		BYTE	szKey[DEFAULT_KEY_LEN];
	}*pDataOut;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	memcpy(pDataIn->szEncKey, InEncKey, 2*DEFAULT_KEY_LEN);
	
	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysNumber:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_DECRYPT_FILE_KEY, InIndex, szSendData, nToLen, szRecvData, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysNumber:HidTransSendRec error!");
		return nRet;
	}

	//check key_id
	pMsgHead = (MsgHeadT *)szRecvData;
	//check head
	nRet = CheckHead(pMsgHead);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetUserName:CheckHead error!");
		return nRet;
	}
	FuncSwapShort(&(pMsgHead->wMsgLen));

	if(memcmp(pMsgHead->szKeyId, InLoginToken, DEFAULT_KEY_ID_LEN) != 0){
		QKEY_DEBUG("GetFileKeysNumber:check recv key_id error!");
		return QK_ERROR_CHECK;
	}
	
	//decrypt data
	nCryptLen = MAX_BUFF_LEN;
	memset(szPduTmp, 0, MAX_BUFF_LEN);
	nRet = Decrypt(CRYPTO_AES_CBC, szRecvData+sizeof(MsgHeadT), pMsgHead->wMsgLen, szPduTmp, &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("GetFileKeysNumber:Decrypt error!");
		return nRet;
	}

	if(nCryptLen != sizeof(struct DataOutTag)){
		QKEY_DEBUG("GetFileKeysNumber:check recv msg error!");
		return QK_ERROR_CHECK;
	}
	
	pDataOut = (struct DataOutTag*)szPduTmp;
	if(pDataOut->sDevId.bDevIdLen != InDevideLen || memcmp(pDataOut->sDevId.szDevId, InDeviceId, InDevideLen) != 0){
		QKEY_DEBUG("GetFileKeysNumber:CheckDevId error!");
		return QK_ERROR_CHECK;
	}

	memcpy(OutKeyId, pDataOut->szKeyId, DEFAULT_KEY_ID_LEN);
	memcpy(OutKey, pDataOut->szKey, DEFAULT_KEY_LEN);
	
	return QK_SUCCESS;
}

int DeleteFileKey(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned short wCount)
{
	MsgHeadT *pMsgHead = NULL;
	int nRet = QK_SUCCESS;
	int nRecLen = 0, nToLen = 0;
	BYTE szPduTmp[MAX_BUFF_LEN] = {0};
	BYTE szSendData[MAX_BUFF_LEN] = {0};
#pragma pack(1)
	struct DataInTag{
		DevIdT sDevId;
		WORD	wCount;
	}*pDataIn;
#pragma pack()
	int nCryptLen = MAX_BUFF_LEN - sizeof(MsgHeadT);
	
	/*init send data*/
	//init body
	pDataIn = (struct DataInTag *)szPduTmp;
	pDataIn->sDevId.bDevIdLen = InDevideLen;
	memcpy(pDataIn->sDevId.szDevId, InDeviceId, DEFAULT_DEV_ID_LEN);
	pDataIn->wCount = wCount;
	//FuncSwapShort(&(pDataIn->wCount));

	//encrypt data
	nRet = Encrypt(CRYPTO_AES_CBC, szPduTmp, sizeof(struct DataInTag), szSendData+sizeof(MsgHeadT), &nCryptLen, InLoginToken + DEFAULT_KEY_ID_LEN, DEFAULT_KEY_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("SetUserName:Encrypt error!");
		return nRet;
	}
	
	//init head
	pMsgHead = (MsgHeadT *)szSendData;
	InitHead(pMsgHead, CRYPTO_AES_CBC, nCryptLen, InLoginToken);

	//send and recv
	nRecLen = 0;
	nToLen = sizeof(MsgHeadT) + nCryptLen;
	nRet = HidTransSendRec(FC_DECRYPT_DELETE_KEY, InIndex, szSendData, nToLen, NULL, &nRecLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("SetUserName:HidTransSendRec error!");
		return nRet;
	}
	
	return QK_SUCCESS;
}



