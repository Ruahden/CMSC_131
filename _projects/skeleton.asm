;to insert title: title *insert title here*

.model small
.data 
.stack 100h
.code
	main proc ;int main
	
	mov ax, @data ;initialization
	mov ds, ax
	
	mov ax, 4c00h ;return 0
	int 21h
		
	main endp ;closing curly brace of main
	end main
	
	;variables db = 1 byte; dw = 2 bytes; dd = 4 bytes
	;mov copies value of 2nd operand to 1st , source and destination must agree in size
	;lea = load effective address, gets 1st element of 2nd operand and stores it to 1st operand