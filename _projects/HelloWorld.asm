title Exer 1

; @author Ruahden F. Dang-awan
; 2012-22241
; CMCS 131 Lab 1a

.model small
.data 
	hello db "Hello World!", '$'

.stack 100h
.code
	main proc ;int main
	
	mov ax, @data ;initialization
	mov ds, ax
	
	lea dx, hello
	mov ah, 09h
	int 21h
	
	mov ax, 4c00h ;return 0
	int 21h
		
	main endp ;closing curly brace of main
	end main