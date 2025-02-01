@16384
D=A
@addr
M=D

@R0
D=M
@n
M=D

@i
M=0

(LOOP)
  @n
  D=M
  @i
  D=D-M  //n - i
  @STOP
  D;JEQ
  
  @addr
  A=M
  M=-1
  D=A
  @32
  D=D+A
  @addr
  M=D
  @i
  M=M+1
  @LOOP
  0;JMP

(STOP)
  @STOP
  0;JMP 
