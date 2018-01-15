/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cn_qtec_qkcl_access_auth_client_jni_QKeyService */

#ifndef _Included_jni_QKeyService
#define _Included_jni_QKeyService
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    genStartInfo
 * Signature: ()Lcn/qtec/qkcl/access/auth/client/jni/QKeyService;
 */
JNIEXPORT jobject JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_genStartInfo
  (JNIEnv *, jobject);

/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    genAuthInfo
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_genAuthInfo
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    checkAuthInfo
 * Signature: ([B)Lcn/qtec/qkcl/access/auth/client/jni/QKeyService;
 */
JNIEXPORT jobject JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_checkAuthInfo
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    encryptQuantumKeys
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_encryptQuantumKeys
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    decryptQuantumKeys
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_decryptQuantumKeys
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    updateRootKey
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_updateRootKey
  (JNIEnv *, jobject);

/*
 * Class:     cn_qtec_qkcl_access_auth_client_jni_QKeyService
 * Method:    confirmRootKey
 * Signature: ([B)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_QKeyService_confirmRootKey
  (JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
