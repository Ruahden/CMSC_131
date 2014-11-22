title HelloWorld

.model small
.stack 100h
.data
	decimal db 12.12
	hexa db 0ah
	hexa2 db 21h
	whatever db 02h
	whatever2 db 09h
	hexa3 db 0BH
	counter db 1
	var db ?y
	hello db "Hello World!", "$"
	anotherHello db "Hello Earl!","$"
.code

	main    proc
   
	mov ax, @data
	mov ds, ax
		
		mov dl, al
		mov al, 'a' 
		add al, counter 
		sub al, hexa
		
		mov var, 0
		mov var, 12
		mov dl, al
		mov ah, 02h
		int 21h
		
		lea dx, hello
		mov ah, 09h
		int 21h
		
		mov dx, offset anotherHello
		mov ah, 09h
		int 21h
	
		mov dl, hexa
		mov ah, 02h
		int 21h
		
	mov ax, 4c00h
	int 21h

	main    endp
	end main
