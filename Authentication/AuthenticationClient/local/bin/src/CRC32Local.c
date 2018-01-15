#include <stdio.h>
#include <jni.h>
#include "cn_qtec_qkcl_access_auth_client_jni_CRC32Local.h"

JNIEXPORT jlong JNICALL Java_cn_qtec_qkcl_access_auth_client_jni_CRC32Local_getCrcValue
  (JNIEnv *env, jobject obj, jbyteArray j_data)
{
      //获取java端传入参数
      jbyte *jbyte_data = (*env)->GetByteArrayElements(env, j_data, NULL);
      jbyte jbyte_dataLength = (*env)->GetArrayLength(env, j_data);

      if(jbyte_data == NULL){
      	    printf("JNI GetByteArrayElements FAILED, Received jbyte_data NULL!\n");
      	    return NULL;
      }

      char *c_data= (char *)jbyte_data;
      UINT

}

static void init_crc_table()
{
    unsigned int c;
    unsigned int i, j;

    for (i = 0; i < 256; i++) {
        c = (unsigned int)i;
        for (j = 0; j < 8; j++) {
            if (c & 1)
                c = 0xedb88320L ^ (c >> 1);
            else
                c = c >> 1;
        }
        crc_table[i] = c;
    }
}

/*计算buffer的crc校验码*/
static unsigned int crc32(unsigned int crc,unsigned char *buffer, unsigned int size)
{
    unsigned int i;
    for (i = 0; i < size; i++) {
        crc = crc_table[(crc ^ buffer[i]) & 0xff] ^ (crc >> 8);
    }
    return crc ;
}