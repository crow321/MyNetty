#include <stdio.h>
#include <jni.h>
#include "jni_QKeyService.h"
#include "Qkey.h"

#undef JNI_BYTE_LENGTH
#define JNI_BYTE_LENGTH      		0x10
#undef JNI_Q_SHIELD_INFO_LENGTH
#define JNI_Q_SHIELD_INFO_LENGTH    0x44

#undef JNI_DEVICE_ID_LENGTH
#define JNI_DEVICE_ID_LENGTH        JNI_BYTE_LENGTH / 2
#undef JNI_SESSION_KEY_LENGTH
#define JNI_SESSION_KEY_LENGTH 		JNI_BYTE_LENGTH * 2
#undef JNI_ROOTKEY_LENGTH
#define JNI_ROOTKEY_LENGTH  		JNI_BYTE_LENGTH * 3
#undef JNI_ENCRYPTED_DATA_LENGTH
#define JNI_ENCRYPTED_DATA_LENGTH  	JNI_BYTE_LENGTH * 1024

JNIEXPORT jobject JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_genStartInfo(
	JNIEnv *env, 
	jobject obj)
{
	//分配内存
    unsigned char c_OutDeviceId[JNI_DEVICE_ID_LENGTH];
    unsigned char OutDeviceIdLen = JNI_DEVICE_ID_LENGTH;
	unsigned char c_OutStartAuthInfo[JNI_Q_SHIELD_INFO_LENGTH];
	unsigned char OutStartAuthInfoLen = JNI_Q_SHIELD_INFO_LENGTH;
    
    //调用Q盾接口 GenStartInfo
	int res = GenStartInfo(c_OutDeviceId, &OutDeviceIdLen, c_OutStartAuthInfo, &OutStartAuthInfoLen);
	if(res != 0){
	    printf("[JNI_QKey_SDK]genStartInfo: Calling GenStartInfo FAILED! 0x%02x\n", res);
	    return NULL;
	}

	//set byteArray to return to java
	jbyteArray jbyteArray_OutDeviceId = (*env)->NewByteArray(env, OutDeviceIdLen);
	(*env)->SetByteArrayRegion(env, jbyteArray_OutDeviceId, 0, OutDeviceIdLen, c_OutDeviceId);
    jbyteArray jbyteArray_OutStartAuthInfo = (*env)->NewByteArray(env, OutStartAuthInfoLen);
    (*env)->SetByteArrayRegion(env, jbyteArray_OutStartAuthInfo, 0, OutStartAuthInfoLen, c_OutStartAuthInfo);


	//获取Java中的类 SessionInfo
	jclass  jclass_QKeyService = (*env)->FindClass(env, "cn/qtec/qkcl/access/auth/client/jni/QKeyService");
	if (jclass_QKeyService == NULL) {
		printf("[JNI_QKey_SDK]genStartInfo:JNI FindClass SessionInfo From JAVA FAILED!\n");
		return NULL;
	}

	//获得得构造函数Id
	jmethodID jmethodID_construct = (*env)->GetMethodID(env, jclass_QKeyService, "<init>", "()V");
	if (jmethodID_construct == NULL) {
		printf("[JNI_QKey_SDK]genStartInfo:Get QKeyService construct Failed!\n");
		return NULL;
	}

	//获取对象
	jobject jobject_QKeyService = (jobject)(*env)->NewObject(env, jclass_QKeyService, jmethodID_construct);
	if (jobject_QKeyService == NULL) {
		printf("[JNI_QKey_SDK]genStartInfo:JNI NewObject SessionInfo Failed!\n");
		return NULL;
	}

	//获取类中每一个变量的定义
	jfieldID jfieldID_deviceID = (*env)->GetFieldID(env, jclass_QKeyService, "deviceID", "[B");
	jfieldID jfieldID_startAuthInfo = (*env)->GetFieldID(env, jclass_QKeyService, "startAuthInfo", "[B");
	if (jfieldID_deviceID == NULL) {
		printf("[JNI_QKey_SDK]genStartInfo:JNI GetFieldID deviceID From JAVA FAILED!\n");
		return NULL;
	}
	else if (jfieldID_startAuthInfo == NULL) {
		printf("[JNI_QKey_SDK]genStartInfo:JNI GetFieldID startAuthInfo From JAVA FAILED!\n");
		return NULL;
	}

	//JNI设置属性值
	(*env)->SetObjectField(env, jobject_QKeyService, jfieldID_deviceID, jbyteArray_OutDeviceId);
	(*env)->SetObjectField(env, jobject_QKeyService, jfieldID_startAuthInfo, jbyteArray_OutStartAuthInfo);

	return jobject_QKeyService;
}

JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_genAuthInfo(
	JNIEnv *env, 
	jobject obj, 
	jbyteArray j_InChallengeInfo)
{
	//获取java端传入参数
	jbyte *jbyte_InChallengeInfo = (*env)->GetByteArrayElements(env, j_InChallengeInfo, NULL);
	jbyte jbyte_InChallengeInfoLength = (*env)->GetArrayLength(env, j_InChallengeInfo);
	
	if(jbyte_InChallengeInfo == NULL){
		printf("[JNI_QKey_SDK]genAuthInfo:JNI GetByteArrayElements FAILED, Received InChallengeInfo NULL!\n");
		return NULL;
	}
	
	unsigned char *c_InChallengeInfo= (unsigned char *)jbyte_InChallengeInfo;

	//调用Q盾 GenAuthInfo 接口
	unsigned char c_OutReqAuthInfo[JNI_Q_SHIELD_INFO_LENGTH] = {0};
	unsigned char c_OutReqAuthInfoLength = (char)JNI_Q_SHIELD_INFO_LENGTH;
	
	int res = GenAuthInfo(c_InChallengeInfo, jbyte_InChallengeInfoLength, c_OutReqAuthInfo, &c_OutReqAuthInfoLength);
	if(res != 0){
		printf("[JNI_QKey_SDK]genAuthInfo:Calling GenAuthInfo FAILED! 0x%02x\n", res);
		return NULL;
	}

	//设置返回的jbyteArray变量
    jbyteArray jbyteArray_OutReqAuthInfo = (*env)->NewByteArray(env, c_OutReqAuthInfoLength);
	(*env)->SetByteArrayRegion(env, jbyteArray_OutReqAuthInfo, 0, c_OutReqAuthInfoLength, c_OutReqAuthInfo);
	
	
	//释放指针
    (*env)->ReleaseByteArrayElements(env, j_InChallengeInfo, jbyte_InChallengeInfo, 0);

	return jbyteArray_OutReqAuthInfo;
}
  
JNIEXPORT jobject JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_checkAuthInfo(
	JNIEnv *env, 
	jobject obj, 
	jbyteArray j_InAuthInfo)
{
	//获取java端传入参数
	jbyte *jbyte_InAuthInfo = (*env)->GetByteArrayElements(env, j_InAuthInfo, NULL);
	jbyte jbyte_InAuthInfoLength = (*env)->GetArrayLength(env, j_InAuthInfo);
	
	if(jbyte_InAuthInfo == NULL){
		printf("[JNI_QKey_SDK]checkAuthInfo:JNI GetByteArrayElements FAILED, Received InAuthInfo NULL!\n");
		return NULL;
	}
	
	unsigned char *c_InAuthInfo = (char *)jbyte_InAuthInfo;
	
	//调用Q盾 CheckAuthInfo 接口
	unsigned char OutSessionId[JNI_SESSION_KEY_LENGTH/2];
	unsigned char OutSessionKey[JNI_SESSION_KEY_LENGTH];
	char OutSessionKeyLen = (char)JNI_SESSION_KEY_LENGTH;
	
	int res = CheckAuthInfo(c_InAuthInfo, jbyte_InAuthInfoLength, OutSessionId, OutSessionKey, &OutSessionKeyLen);
	if(res != 0){
		printf("[JNI_QKey_SDK]checkAuthInfo:Calling CheckAuthInfo FAILED! 0x%02x\n", res);
		return NULL;
	}

	//创建jbyteArray
	jbyteArray jbyteArray_OutSessionId = (*env)->NewByteArray(env, JNI_SESSION_KEY_LENGTH/2);
	jbyteArray jbyteArray_OutSessionKey = (*env)->NewByteArray(env, JNI_SESSION_KEY_LENGTH);
	(*env)->SetByteArrayRegion(env, jbyteArray_OutSessionId, 0, JNI_SESSION_KEY_LENGTH/2, OutSessionId);
	(*env)->SetByteArrayRegion(env, jbyteArray_OutSessionKey, 0, JNI_SESSION_KEY_LENGTH, OutSessionKey);

	//获取Java中的类 SessionInfo
	jclass  jclass_QKeyService = (*env)->FindClass(env, "cn/qtec/qkcl/access/auth/client/jni/QKeyService");
    if (jclass_QKeyService == NULL){
	    printf("[JNI_QKey_SDK]checkAuthInfo:JNI FindClass SessionInfo From JAVA FAILED!\n");
		return NULL;
	}

	//获得得构造函数Id
    jmethodID jmethodID_construct = (*env)->GetMethodID(env, jclass_QKeyService, "<init>","()V");
    if (jmethodID_construct == NULL){
        printf("[JNI_QKey_SDK]checkAuthInfo:Get QKeyService construct Failed!\n");
        return NULL;
    }

    //获取对象
    jobject jobject_QKeyService =  (jobject)(*env)->NewObject(env, jclass_QKeyService, jmethodID_construct);
    if (jobject_QKeyService == NULL){
        printf("[JNI_QKey_SDK]checkAuthInfo:JNI NewObject SessionInfo Failed!\n");
        return NULL;
    }

	//获取类中每一个变量的定义
	jfieldID jfieldID_sessionID = (*env)->GetFieldID(env, jclass_QKeyService, "sessionID", "[B");
	jfieldID jfieldID_sessionKey = (*env)->GetFieldID(env, jclass_QKeyService, "sessionKey", "[B");
	if(jfieldID_sessionID == NULL){
		printf("[JNI_QKey_SDK]checkAuthInfo:JNI GetFieldID sessionID From JAVA FAILED!\n");
		return NULL;
	}else if(jfieldID_sessionKey == NULL){
		printf("[JNI_QKey_SDK]checkAuthInfo:JNI GetFieldID sessionKey From JAVA FAILED!\n");
		return NULL;
	}

	//JNI设置属性值
	(*env)->SetObjectField(env, jobject_QKeyService, jfieldID_sessionID, jbyteArray_OutSessionId);
	(*env)->SetObjectField(env, jobject_QKeyService, jfieldID_sessionKey, jbyteArray_OutSessionKey);

	//释放指针
	(*env)->ReleaseByteArrayElements(env, j_InAuthInfo, jbyte_InAuthInfo, 0);

	return jobject_QKeyService;
}
  
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_encryptQuantumKeys(
	JNIEnv *env, 
	jobject obj, 
	jbyteArray j_InputData)
{
	//获取java端传入参数
	jbyte *jbyte_InputData = (*env)->GetByteArrayElements(env, j_InputData, NULL);
	jint jint_InputDataLength = (*env)->GetArrayLength(env, j_InputData);
	
	if (jbyte_InputData == NULL){
		printf("[JNI_QKey_SDK]encryptQuantumKeys:JNI GetByteArrayElements FAILED, Received InputData NULL!\n");
		return NULL;
	}
	
	unsigned char *c_InputData = (char *)jbyte_InputData;

	//释放指针
	(*env)->ReleaseByteArrayElements(env, j_InputData, jbyte_InputData, 0);

	//调用Q盾 EncryptQuantumKeys 接口
	unsigned char c_EncryptedData[JNI_ENCRYPTED_DATA_LENGTH];
	unsigned int c_EncryptedDataLength = (int) JNI_ENCRYPTED_DATA_LENGTH;

	int res = EncryptQuantumKeys(c_InputData, jint_InputDataLength, c_EncryptedData, &c_EncryptedDataLength);
	if(res != 0){
		printf("[JNI_QKey_SDK]encryptQuantumKeys:Calling EncryptQuantumKeys FAILED! 0x%02x\n", res);
		return NULL;
	}

	//将加密数据转为Java byte数组
	jbyteArray cbyte_EncryptedData = (*env)->NewByteArray(env,c_EncryptedDataLength);
	(*env)->SetByteArrayRegion(env, cbyte_EncryptedData, 0, c_EncryptedDataLength, c_EncryptedData);
	
	return cbyte_EncryptedData;
	
}
  
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_decryptQuantumKeys(
	JNIEnv *env, 
	jobject obj, 
	jbyteArray j_InEncryptedData)
{
	//获取java端传入参数
	jbyte *jbyte_InEncryptedData = (*env)->GetByteArrayElements(env, j_InEncryptedData, NULL);
	jint jint_InEncryptedDataLength = (*env)->GetArrayLength(env, j_InEncryptedData);
	
	if(jbyte_InEncryptedData == NULL){
		printf("[JNI_QKey_SDK]decryptQuantumKeys:JNI GetByteArrayElements FAILED, Received InEncryptedData NULL!\n");
		return NULL;
	}
	
	unsigned char *c_InEncryptedData = (char *)jbyte_InEncryptedData;

	//释放指针
	(*env)->ReleaseByteArrayElements(env, j_InEncryptedData, jbyte_InEncryptedData, 0);

	//调用Q盾 DecryptQuantumKeys 接口
	unsigned char c_DecryptedKeyInfo[JNI_ENCRYPTED_DATA_LENGTH];
	unsigned int c_DecryptedKeyInfoLength = (int) JNI_ENCRYPTED_DATA_LENGTH;

	int res = DecryptQuantumKeys(c_InEncryptedData, jint_InEncryptedDataLength, c_DecryptedKeyInfo, &c_DecryptedKeyInfoLength);

	if(res != 0){
		printf("[JNI_QKey_SDK]decryptQuantumKeys:Calling DecryptQuantumKeys FAILED! 0x%02x\n", res);
		return NULL;
	}

	//将加密数据转为Java byte数组
	jbyteArray cbyte_DecryptedKeyInfo = (*env)->NewByteArray(env,c_DecryptedKeyInfoLength);
	(*env)->SetByteArrayRegion(env, cbyte_DecryptedKeyInfo, 0, c_DecryptedKeyInfoLength, c_DecryptedKeyInfo);
	
	return cbyte_DecryptedKeyInfo;
}
  
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_updateRootKey(
	JNIEnv *env, 
	jobject obj)
{
	//调用Q盾 UpdateRootKey 接口
	unsigned char c_RootKey[JNI_ROOTKEY_LENGTH];
	unsigned char c_RootKeyLength = (unsigned char) JNI_ROOTKEY_LENGTH;

	int res = UpdateRootKey(c_RootKey, &c_RootKeyLength);
	if(res != 0){
		printf("[JNI_QKey_SDK]updateRootKey:Calling UpdateRootKey FAILED! 0x%02x\n", res);
		return NULL;
	}

	//将加密数据转为Java byte数组
	jbyteArray cbyte_RootKey = (*env)->NewByteArray(env, c_RootKeyLength);
	(*env)->SetByteArrayRegion(env, cbyte_RootKey, 0, c_RootKeyLength, c_RootKey);
	
	return cbyte_RootKey;
}
  
JNIEXPORT jboolean JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_confirmRootKey(
	JNIEnv *env, 
	jobject obj, 
	jbyteArray j_RootKey)
{	
	//返回结果 0 false ,1 true
	unsigned char result = 0;
	
	//获取java端传入参数
	jbyte *jbyte_RootKey = (*env)->GetByteArrayElements(env, j_RootKey, NULL);
	jbyte jbyte_RootKeyLength = (*env)->GetArrayLength(env, j_RootKey);
	
	if (jbyte_RootKey == NULL){
		printf("[JNI_QKey_SDK]confirmRootKey:JNI GetByteArrayElements FAILED, Received RootKey NULL!\n");
		return (jboolean) result;
	}
	
	unsigned char *c_RootKey = (char *)jbyte_RootKey;

	//释放指针
	(*env)->ReleaseByteArrayElements(env, j_RootKey, jbyte_RootKey, 0);

	//调用Q盾 DecryptQuantumKeys 接口
	int res = ConfirmRootKey(c_RootKey, jbyte_RootKeyLength);

	//检查返回结果
	if(res != 0){
		printf("[JNI_QKey_SDK]confirmRootKey:Calling ConfirmRootKey FAILED! 0x%02x\n", res);
		return (jboolean) result;
	}

	result = 1;
	return (jboolean) result;
}
