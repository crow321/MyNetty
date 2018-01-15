#include "Qkey.h"
#include "HidTrans.h"
#include "hidapi.h"

/*Some function*/
int PrintData(unsigned char *pName, unsigned char *pData, int nLen)
{
#if 1
	int i = 0;
	printf("%s len:%d\n", pName, nLen);
	for (i = 0; i < nLen; i++)
		printf("%02hhx ", pData[i]);
	printf("\n");
#endif
	return 0;
}

void FuncSwapShort(WORD* p_value)
{
	WORD val = *p_value;

	*p_value = (((val & 0xff)<<8) | (val >> 8));
}

int IsLittleEndian()
{
	union uTemp
	{
		short int i;
		char c;
	}pTemp;
	
	pTemp.i = 0x1234;
	return (pTemp.c == 0x34);
}

/*Add short to data--little endian*/
int FcLittleEndianAddShortToData(BYTE *pData, const WORD wLen)
{
	if(NULL == pData)
		return QK_ERROR_ARG;

	if(IsLittleEndian()){
		*pData = LOWSHORT(wLen);
		*(pData + 1) = HIGHSHORT(wLen);
	}
	else{
		*pData = HIGHSHORT(wLen);
		*(pData + 1) = LOWSHORT(wLen);
	}
	
	return QK_SUCCESS;
}

/*Get device count*/
int HidTransGetCount()
{
	int nCount = 0;
	struct hid_device_info *devs, *cur_dev;

	if (hid_init())
		return nCount;

	/*Get QKey Device*/
	devs = hid_enumerate(0x0, 0x0);
	cur_dev = devs;	
	while (cur_dev) {
		if(cur_dev->vendor_id == QKEY_VENDOR_ID && QKEY_PRODUCT_ID == cur_dev->product_id){
			nCount++;
			QKEY_DEBUG_INFO("Device Found:");
			QKEY_DEBUG_INFO("  type: %04hx %04hx  path: %s  serial_number: %ls", cur_dev->vendor_id, cur_dev->product_id, cur_dev->path, cur_dev->serial_number);
			QKEY_DEBUG_INFO("  Manufacturer: %ls", cur_dev->manufacturer_string);
			QKEY_DEBUG_INFO("  Product:		%ls", cur_dev->product_string);
			QKEY_DEBUG_INFO("  Release:		%hx", cur_dev->release_number);
			QKEY_DEBUG_INFO("  Interface:	%d",	cur_dev->interface_number);
		}
		
		cur_dev = cur_dev->next;
	}
	hid_free_enumeration(devs);	

	
	return nCount;
}

/*Open usb devide*/
int HidTransOpenDevide(hid_device **pHandle, int nIndex)
{
	int nFind = nIndex + 1;
	struct hid_device_info *devs, *cur_dev;
	char *pPath = NULL;

	if(nIndex < 0){
		QKEY_DEBUG("HidTransOpenDevide:Open Device argument error, nIndex must bigger or equal than 0,nIndex = %d", nIndex);
		return QK_ERROR_FOUND;
	}

	/*Init*/
	if (hid_init())
		return QK_ERROR_INITIALIZED;

	/*Get QKey Device*/
	devs = hid_enumerate(0x0, 0x0);
	cur_dev = devs;	
	while (cur_dev) {
		if(cur_dev->vendor_id == QKEY_VENDOR_ID && QKEY_PRODUCT_ID == cur_dev->product_id){
			nFind--;
			if(nFind == 0){
				pPath = cur_dev->path;
				break;
			}
		}
		
		cur_dev = cur_dev->next;
	}

	if(nFind == 0 && pPath){
		*pHandle = NULL;
		//*pHandle = hid_open(QKEY_VENDOR_ID, QKEY_PRODUCT_ID, pSerialNum);	/*Don't need to worry about one machine Contains more than one quantum usb-key*/
		*pHandle = hid_open_path(pPath);
		if (!*pHandle) {
			hid_free_enumeration(devs); 
			QKEY_DEBUG("HidTransOpenDevide:Open Device error!");
	 		return QK_ERROR_INITIALIZED;
		}
	}
	else{
		hid_free_enumeration(devs); 
		QKEY_DEBUG("HidTransOpenDevide find Device error!");
		return QK_ERROR_FOUND;
	}
	
	hid_free_enumeration(devs);	
	
	return QK_SUCCESS;
}

/*Close usb device*/
int HidTransCloseDevice(hid_device *pHandle)
{
	hid_close(pHandle);
	hid_exit();
	
	return QK_SUCCESS;
}

/*Add header to send buffer(2Bit FC|2Bit LEN|2Bit Sta.|DATA)*/
int HidTransDataAddHeader(const EQkFunCode eFunCode, const BYTE *pSrcData, const UINT nSrcLen, BYTE *pDstData)
{
	int nOffset = 0;
	if(NULL == pDstData){
		QKEY_DEBUG("HidTransDataAddHeader argument error!");
		return QK_ERROR_ARG;
	}
	
	/*Little-Endian*/
	memset(pDstData, 0, (nSrcLen + DATA_HEAD_LEN + DEFAULT_REPORT_ID_LEN));
	pDstData[nOffset] = DEFAULT_REPORT_ID;	//hid trans need this Bit
	nOffset++;
	
	pDstData[nOffset] = (BYTE)eFunCode;	//fun code
	nOffset++;

	pDstData[nOffset] = MD(eFunCode) + DC_CLIENT_TO_QSHELL;	//add direction and mode
	nOffset++;

	FcLittleEndianAddShortToData(pDstData + nOffset, (WORD)(nSrcLen + HEAD_STATE_LEN));	//len
	nOffset += 2;
	
	FcLittleEndianAddShortToData(pDstData + nOffset, (WORD)0x00);	//len
	nOffset += 2;
	
	memcpy(pDstData + nOffset, pSrcData, nSrcLen);	//data

	return QK_SUCCESS;
}

/*Check header and delete header*/
int HidTransDataDelHeader(const EQkFunCode eFunCode, BYTE *pSrcData, UINT *pSrcLen)
{
	WORD wLen = 0;
	BYTE *pTmp = NULL;
	if(NULL == pSrcData || 0 == *pSrcLen)
		return QK_ERROR_ARG;

	/*Check funcode*/
	if(LW_FC(pSrcData) != (BYTE)eFunCode){
		QKEY_DEBUG("HidTransDataDelHeader check function code error, "\
			"LW_FC(pSrcData) = %d, eFunCode = %d.", LW_FC(pSrcData), eFunCode);
		return QK_ERROR_CHECK;
	}

	/*Check direction and mode*/
	if(DC(pSrcData) !=  (MD(eFunCode) + DC_QSHELL_TO_CLIENT)){
		QKEY_DEBUG("HidTransDataDelHeader check direction error!");
		return QK_ERROR_CHECK;
	}

	/*Get length and check*/
	wLen = LEN(pSrcData);
	if(*pSrcLen < (UINT)(wLen + DATA_HEAD_LEN - HEAD_STATE_LEN)){
		QKEY_DEBUG("HidTransDataDelHeader check length error!");
		return QK_ERROR_CHECK;
	}
	
	/*Check return state*/
	if(RC(pSrcData) != QK_SUCCESS){
		QKEY_DEBUG("HidTransDataDelHeader check state error!RC(pSrcData) = %04x", RC(pSrcData));
		return RC(pSrcData);
	}
	
	/*Delete header and get data*/
	pTmp = MALLOC(*pSrcLen, BYTE);
    if(!pTmp){
		QKEY_DEBUG("HidTransDataDelHeader malloc error!");
    	return QK_ERROR_MALLOC;
    }
    memcpy(pTmp, pSrcData + DATA_HEAD_LEN, wLen);
    memset(pSrcData, 0, *pSrcLen);
	*pSrcLen = wLen;
    memcpy(pSrcData, pTmp, *pSrcLen);
	
    free(pTmp);
	
	return QK_SUCCESS;
}

int HidTransWriteData(hid_device *pHandle, BYTE *pSendData, UINT nSendLen)
{
	UINT nCount = 0;
	if(NULL == pHandle || NULL == pSendData || 0 == nSendLen){
		QKEY_DEBUG("HidTransWriteData argument error");
	}
	
	nCount = hid_write(pHandle, pSendData, (size_t)nSendLen);
	if(nCount < nSendLen){
		QKEY_DEBUG("HidTransSendRec:hid_write error!");
		//printf("HidTransSendRec:hid_write error!nCount = %d\n", nCount);
		return QK_ERROR_TRANSFER;
	}

	return QK_SUCCESS;
}

int HidTransReadData(hid_device *pHandle, BYTE *pRecvData, UINT *pRecvLen)
{
	int nRes = 0, nRecvCount = 0;
	BYTE szBuf[DEFAULT_TRANS_LEN] = {0};
	if(NULL == pHandle || NULL == pRecvData || 0 == *pRecvLen){
		QKEY_DEBUG("HidTransReadData argument error");
	}

	memset(pRecvData, 0, *pRecvLen);
	while(nRecvCount < DEFAULT_TRANS_LEN){
		memset(szBuf, 0, DEFAULT_TRANS_LEN);
		nRes = hid_read_timeout(pHandle, szBuf, DEFAULT_TRANS_LEN, MAX_TIME_OUT);	//500ms time out
		//nRes = hid_read_timeout(pHandle, szBuf, DEFAULT_TRANS_LEN, -1);
		if(nRes){
			if(*pRecvLen >= (UINT)(nRecvCount + nRes)){
				memcpy(pRecvData + nRecvCount, szBuf, nRes);
				nRecvCount += nRes;
			}
			else{
				QKEY_DEBUG("HidTransReadData:hid_read_timeout recv buffer len is less than recieve buffer!nRecvLen + nRes = %d, DEFAULT_TRANS_LEN = %d", nRecvCount + nRes, DEFAULT_TRANS_LEN);
				return QK_ERROR_ARG;
			}
		}
		else{
			QKEY_DEBUG("HidTransReadData:hid_read_timeout no more buffer to read!");
			break;
		}
	}

	/*Check read buffer*/
	if(nRecvCount < DATA_HEAD_LEN){
		QKEY_DEBUG("HidTransReadData:hid_read_timeout read buffer error!");
		return QK_ERROR_TRANSFER;
	}

	*pRecvLen = nRecvCount;

	return QK_SUCCESS;	
}


int HidTransSendPDU(hid_device *pHandle, const EQkFunCode eFunCode, const BYTE *pSendPDU, const UINT nSendPDULen)
{
	int nRet = QK_SUCCESS;
	BYTE szSendData[DEFAULT_TRANS_LEN] = {0};
	
	/*Add header*/
	nRet = HidTransDataAddHeader(eFunCode, pSendPDU, nSendPDULen, szSendData);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("HidTransSendPDU:QKeyAddHeader error!");
		return nRet;
	}

	/*Write buffer*/
	nRet = HidTransWriteData(pHandle, szSendData, (size_t)DEFAULT_TRANS_LEN);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("HidTransSendPDU:HidTransWriteData error!");
		return nRet;
	}

	return QK_SUCCESS;
}

int HidTransRecvPDU(hid_device *pHandle, const EQkFunCode eFunCode, BYTE *pRecvPDU, UINT *pRecvPDULen)
{
	int nRet = QK_SUCCESS;
	UINT nRecvLen = DEFAULT_TRANS_LEN;
	BYTE szRecvData[DEFAULT_TRANS_LEN] = {0};

	/*Read buffer*/
	nRet = HidTransReadData(pHandle, szRecvData, &nRecvLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("HidTransRecvPDU:HidTransReadData recv buffer error!");
		return nRet;
	}

	/*Delete and check header*/
	nRet = HidTransDataDelHeader(eFunCode, szRecvData, &nRecvLen);
	if(QK_SUCCESS != nRet){
		QKEY_DEBUG("HidTransRecvPDU:HidTransDataDelHeader error!");
		return nRet;
	}

	if(*pRecvPDULen){
		memset(pRecvPDU, 0, *pRecvPDULen);
	}

	/*Copy data out */
	if(nRecvLen > 0){
		if(*pRecvPDULen > nRecvLen){
			memcpy(pRecvPDU, szRecvData, nRecvLen);
			*pRecvPDULen = nRecvLen;
		}
		else{
			if(*pRecvPDULen){	//want to recv data, but recv buff length is not enough 
				QKEY_DEBUG("HidTransRecvPDU error pRecvData length is less than recv buffer!");
				return QK_ERROR_ARG;
			}
			//else do not want to read any data
		}
	}
	else{
		*pRecvPDULen = 0;	//recv pdu is null 
	}
		
	return QK_SUCCESS;
}

int HidTransSendRec(const EQkFunCode eFunCode, int nIndex, const BYTE *pSendData, const UINT nSendLen, BYTE *pRecvData, UINT *pRecvLen)
{
	INT nRet = QK_SUCCESS, nCount = 0;
	hid_device *pHandle = NULL;
	if(nSendLen > (DEFAULT_TRANS_LEN - DATA_HEAD_LEN - DEFAULT_REPORT_ID_LEN)){
		QKEY_DEBUG("HidTransSendRec argument error, send buffer too long! send len: %d", nSendLen);
		return QK_ERROR_ARG;
	}


	/*Open device*/
	nRet = HidTransOpenDevide(&pHandle, nIndex);
	if(QK_SUCCESS != nRet || NULL == pHandle){
		QKEY_DEBUG("HidTransSendRec:HidTransOpenDevide error!");
		return nRet;
	}

	/*Send PDU data*/
	nRet = HidTransSendPDU(pHandle, eFunCode, pSendData, nSendLen);
	if(QK_SUCCESS != nRet){
		HidTransCloseDevice(pHandle);/*Close device*/
		QKEY_DEBUG("HidTransSendRec:HidTransSendPDU error!");
		return nRet;
	}

	/*Recv PDU data*/
	nRet = HidTransRecvPDU(pHandle, eFunCode, pRecvData, pRecvLen);
	if(QK_SUCCESS != nRet){
		HidTransCloseDevice(pHandle);/*Close device*/
		QKEY_DEBUG("HidTransSendRec:HidTransRecvPDU error!");
		return nRet;
	}
	
	/*Close device*/
	HidTransCloseDevice(pHandle);

	return QK_SUCCESS;
}

