EXEC_A = libjni.a
EXEC_SO = libjni.so

SRCS = $(wildcard *.c)
OBJS = $(patsubst %.c, %.o, $(SRCS))

CFLAGS += -fPIC
LDFLAGS += -shared
INC += -I./ -I../inc
LIBS += -lusb-1.0
LIB_PATH += ./lib
LIB += -L/../

.PHONY: all clean

all : $(EXEC_A) $(EXEC_SO)

$(EXEC_A) : $(OBJS)
	$(AR) cr $(LIB_PATH)/$@ $^

$(EXEC_SO) : $(OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) -o $(LIB_PATH)/$@ $^ $(INC) $(LIB) $(LIBS)

%.o : %.c
	$(CC) $(CFLAGS) -c $^ -o $@ $(INC)

clean:
	rm -f $(EXEC) *.elf *.gdb *.o $(LIB_PATH)/*.so $(LIB_PATH)/*.a
