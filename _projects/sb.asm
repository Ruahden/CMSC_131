title Decimal to Binary Converter

; @author Ruahden F. Dang-awan
; 2012-22241
; CMCS 131 Lab 1a

.model small
.data 
	answer db 16 dup (0), '$'
	intro db 'Input a number between 0 - 65535. Press Enter when you are done.' ,0ah, '$'
.stack 100h
.code
	
	getInput proc

		newloop:	
			mov ah, 01h
			int 21h ;input
		
		cmp al, 13
		je endpoint
			
			mov ah, 0
			sub al, '0'
			mov bx, ax
			mov ax, 10
			mul cx
			mov cx, ax
			add cx, bx
			
		jmp newloop
	
	endpoint:
	ret
	getInput endp
	
	print proc
		printloop:
		shr ax, 1
		jc maycounter
		mov answer[bx], '0'
		jmp nearend
		maycounter:
		mov answer[bx], '1'
		nearend:
		clc
		inc bx
		cmp bx, 16
		jne printloop
	
		dec bx
		
		printanswer:
			mov dl, answer[bx]
			mov ah, 02h
			int 21h
			xor dx,dx
			mov ax,bx
			mov cx, 4
			div cx
			cmp dx, 0
			jne nospace
				mov dl, 32
				mov ah, 02h
				int 21h
			nospace:
			
			dec bx
			cmp bx,-1
		jne printanswer
		
		
	ret
	print endp

	main proc ;int main
	
		mov ax, @data ;initialization
		mov ds, ax

		lea dx, intro
		mov ah, 09h
		int 21h
		
		xor cx,cx
		call getInput
			
		xor dx,dx
		xor bx,bx
		
		mov ax, cx
		
		call print 
		
		mov ax, 4c00h ;return 0
		int 21h
		
	main endp ;closing curly brace of main
	end main