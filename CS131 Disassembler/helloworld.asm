title HelloWorld

.model small
.stack 100h
.data
	hello db 'Hello World!', '$'
.code

	main    proc
   
	mov ax, @data
	mov ds, ax

		lea dx, hello
		mov ah, 09h
		int 21h
	
	mov ax, 4c00h
	int 21h

	main    endp
	end main