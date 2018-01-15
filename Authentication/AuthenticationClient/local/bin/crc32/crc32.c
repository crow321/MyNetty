static UINT s_POLYNOMIAL;
static int s_have_table;
static UINT s_table[256];

static void Crc32Table()
{
    s_have_table = 1 ;

  int iteration;
    uint32_t crc;
  s_POLYNOMIAL = 0x04C11DB7;

    // Fill CRC32 table
    for (UINT index = 0 ; index < 256 ; ++index)
    {
      for (crc = index << 24, iteration = 8; iteration > 0; iteration--)
      {
        if (crc & 0x80000000)
        {
          crc = (crc << 1) ^ s_POLYNOMIAL;
        }
        else
        {
          crc = (crc << 1);
        }
      }
      s_table[index] = crc;
    }
}

static uint32_t MakeCrc32(char *buff, int len)
{
  uint32_t crc =0xFFFFFFFF;
  s_have_table = 0;
    if (!s_have_table) Crc32Table() ;
    const unsigned char* BUFFER_PTR = (unsigned char*)buff;

    for (int i = len; i > 0; ++BUFFER_PTR, --i)
    {
      crc = (crc << 8) ^ s_table[(crc >> 24) ^ *BUFFER_PTR];
    }
  return ~crc;
}