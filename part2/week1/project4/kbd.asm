(LOOP)
    @i
    M=0  //i=0

    @SCREEN
    D=A
    @addr //addr=SCREEN
    M=D

    @8192
    D=A  //screen = 256 * 512; word = 8. (256*512)/8 = 8192. 8192 addresses = entrire display.
    @n
    M=D

    @KBD
    D=M
    @WHITE
    D;JEQ



    (BLACK)
    @n
    D=M
    @i
    D=D-M  //n - i
    @LOOP
    D;JEQ
  
    @addr
    A=M
    M=-1
    D=A+1
    @addr
    M=D
    @i
    M=M+1
    @BLACK
    0;JMP

    (WHITE)
    @n
    D=M
    @i
    D=D-M  //n - i
    @LOOP
    D;JEQ
  
    @addr
    A=M
    M=0
    D=A+1
    @addr
    M=D
    @i
    M=M+1
    @WHITE
    0;JMP
