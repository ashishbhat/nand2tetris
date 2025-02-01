@R0
D=M
@x
M=D

@R1
D=M
@y
M=D

@prod
M=0

(LOOP)
  @x
  D=M
  @STOP
  D;JEQ
  @y
  D=M
  @prod
  M=D+M
  D=M
  @R2
  M=D
  @x
  M=M-1
  @LOOP
  0;JMP

(STOP)
  @STOP
  0;JMP
  