title Exer 2

; @author Ruahden F. Dang-awan
; 2012-22241
; CMCS 131 Lab 1a

.model small
.data 
	var db 0ah, ?, '$'

.stack 100h
.code
	main proc ;int main
	
	mov ax, @data ;initialization
	mov ds, ax
	
	mov ah, 01h
	int 21h ;input
	
	mov var+1, al
	lea dx, var
	mov ah, 09h
	int 21h ;output
	
	mov ax, 4c00h ;return 0
	int 21h
		
	main endp ;closing curly brace of main
	end main