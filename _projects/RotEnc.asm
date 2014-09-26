title Rot13 Exercise

; @author Ruahden F. Dang-awan
; 2012-22241
; CMCS 131 Lab 1a

.model small
.data 
	string db 0ah ,12 dup(?)
.stack 100h
.code
	main proc ;int main
	
	mov ax, @data ;initialization
	mov ds, ax
	mov bx, 1
	
	inputloop: ;gets string of 10 characters
		mov ah, 01h
		int 21h ;input		
		mov string[bx], al;
		inc bx
		cmp bx, 11; 
	jne inputloop

	mov bx, 1	;initialize value of counter
	mainloop:
	
	cmp string[bx], 93
	jg pgt
	jl plt
	
	plt:
		cmp string[bx], 77
	jg pgss
		add string[bx], 13
	jmp final 
	
	pgss:
	sub string[bx], 77
	add string[bx], 64
	jmp final
	
	pgt:
		cmp string[bx], 109
	jg pgohn
		add string[bx], 13
	jmp final
	
	pgohn:
	sub string[bx], 109
	add string[bx], 96
	jmp final
	
	final:
	inc bx
	cmp bx, 11
	jne mainloop

	mov string +11, '$'
	lea dx, string
	mov ah, 09h
	int 21h
	
	mov ax, 4c00h ;return 0
	int 21h
		
	main endp ;closing curly brace of main
	end main