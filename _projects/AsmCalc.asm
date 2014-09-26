title Assembly Calculator

; @author Ruahden F. Dang-awan
; 2012-22241
; CMCS 131 Lab 1a

.model small
.data 
	intro db 'Input two non-negative 2-digit integers from 00-99. eg. 01, 99. NO NEED TO PRESS ENTER.' ,0ah, '$'
	input db 2 dup(0)
	max db 0ah,'maximum: ' , '$'
	min db 0ah,'minimum: ' , '$'
	sum db 0ah, 'sum: ', '$'
	difference db 0ah,'difference: ', '$'
	product db 0ah,'product: ', '$'
	quotient db 0ah,'quotient: ', '$'
	average db 0ah,'average: ', '$'
	modulo db 0ah,'modulo: ', '$'
	first db 0ah,'first input: ', '$'
	second db 0ah,'second input: ', '$'
	decimalsum db 0
	remainder db 0
	point5string db '.5','$'
	ud db 'undefined' ,'$'
	
.stack 100h
.code
	
	getInput proc
		
		newloop:	
			mov ah, 01h
			int 21h
			
			sub al, '0'
			mov dh, al
			mov al, 10
			mul cl
			mov cl, al
			mov al, dh
			add cl, al
			
			inc bl
			cmp bl, 2			
		jne newloop
		
		mov dl, 0ah
		mov ah, 02h
		int 21h
		
		inc bh
		cmp bh, 2
		je gotoend
		
		mov input[0], cl
		xor bx,bx
		inc bh
		mov cl, 0
		jmp newloop
		
		gotoend:
		mov input[1], cl		
	ret
	getInput endp
	
	
	print proc
		
		xor dx,dx
		mov cx, 1000 ;this will divide ax
		
		cmp ax,0
		jne zero
		mov dl, '0'
		mov ah, 02h
		int 21h
		jmp lastline
		zero:
		
		cmp cx, ax
		jle printloop
		mov bx, ax ;store ax on bx so that value won't be affected by changes
		reduce:
			mov ax, cx
			mov cx, 10
			div cx
			xor dx,dx
			mov cx,ax
		cmp cx, bx
		jg reduce
		
		mov ax,bx
		printloop:
			div cx
			
			mov bx, dx ;saves the remainder on bx
			
			add ax, '0';prints the quotient, assuming that it is only 0-9. it will never reach ah. only al.
			mov dl, al 
			mov ah, 02h
			int 21h
			
			xor dx,dx ;initializes dx for division
			mov ax, cx ;this block divides the current cx by 10. 
			mov cx, 10
			div cx ; error
			mov cx, ax
			
			mov ax,bx ; puts remainder on ax, to be divided on the next loop
			
		cmp cx, 0 ; cx will be zero if the last cx was one. meaning up until the ones was divided. 
		jne printloop		
		
		lastline:
	ret
	print endp
		
		
	main proc ;int main
	
		mov ax, @data ;initialization
		mov ds, ax

		lea dx, intro
		mov ah, 09h
		int 21h
				
		xor dx,dx
		xor cx,cx
		call getInput
		
		lea dx, sum ;prints sum of inputs
		mov ah, 09h
		int 21h
		xor ax,ax
		mov al, input[0]
		add al, input[1]
		mov decimalsum, al
		call print
		
		lea dx, difference ;prints difference of inputs
		mov ah, 09h
		int 21h
		xor ax,ax
		mov al, input[0]
		cmp al, input[1]
		jl biggersecond
		sub al, input[1]
		jmp notbigsecond
		biggersecond:
		mov dl, '-'
		mov ah, 02h
		int 21h
		mov al, input[1]
		sub al, input[0]
		mov ah, 0
		notbigsecond:
		call print
				
		lea dx, product ;prints product of inputs
		mov ah, 09h
		int 21h
		xor ax,ax
		mov al, input[0]
		mul input[1]
		call print
		
		lea dx, quotient ;prints quotient of inputs
		mov ah, 09h
		int 21h
		cmp input[1], 0
		jz undefined
		xor ax,ax
		mov al, input[0]
		div input[1]
		mov remainder, ah ;saves remainder
		mov ah,0
		call print
		jmp defined
		undefined:
		lea dx, ud ;divisor is 0
		mov ah, 09h
		int 21h
		defined:
		
		lea dx, modulo ;prints saved remainder of previous block
		mov ah, 09h
		int 21h
		xor ax,ax
		mov al, remainder
		call print
				
		mov al, input[0] ;gets max and min
		cmp al, input[1]
		jg greaterfirst
		
		lea dx, max
		mov ah, 09h
		int 21h		
				
		xor ax,ax
		mov al, input[1]
		call print

		lea dx, min
		mov ah, 09h
		int 21h
		
		xor ax,ax
		mov al, input[0]
		call print
		
		jmp notfirst
		greaterfirst:
		
		lea dx, max
		mov ah, 09h
		int 21h
		
		xor ax,ax
		mov al, input[0]
		call print

		lea dx, min
		mov ah, 09h
		int 21h
		
		xor ax,ax
		mov al, input[1]
		call print

		notfirst:
		
		lea dx, average ;prints average of inputs
		mov ah, 09h
		int 21h
		xor ax,ax
		mov al, decimalsum
		mov cl, 2
		div cl
		mov remainder, ah ;saves remainder on point5
		xor ah, ah
		call print
		cmp remainder, 1
		jne nopoint5 ;goes into next block if there's a remainder
			lea dx, point5string ;prints '.5'
			mov ah,09h
			int 21h
		nopoint5:
		
		mov ax, 4c00h ;return 0
		int 21h
		
	main endp ;closing curly brace of main
	end main