title Bit_Representation
; James-Andrew R. Sarmiento
; 2012-26531

.model small 
.data
	userInput dw 5 dup(?), '$'
	hundreds db 3 dup(?), '$'
	tens db 2 dup(?), '$'
	ones db 1 dup(?), '$'
	;MSG1 db 10,13, "In", '$'
	shiftArray db 8 dup(0), '$'
	;stringToRotate db 10 dup(0), '$'
	count db 0
	count2 db 0
	value db 0
.stack 100h
.code
	main proc
	
	mov ax, @data
	mov ds,ax
	mov bx, 0
	
	label1:
		
		mov ah, 01h 						;get input from user
	    int 21h
		
		cmp al, 13							; compare if input is a space
		je label2
		
		inc count
		mov userInput[bx], ax				; stores char on array
		sub userInput[bx], '0'
		
		cmp count, 5
		jl label1
	
	label2:
		mov bx, 0

		cmp count, 3
		je threeInputDeclaration
		
		cmp count, 2
		je twoInputDeclaration
		
		cmp count, 1
		je oneInputDeclaration
		
			threeInputDeclaration:
				mov hundreds[0], 100
				mov hundreds[1], 10
				mov hundreds[2], 1
				jmp calculateThreeInput
					calculateThreeInput:
						mov ax, userInput[bx]
						mov bl, hundreds[bx]
						mul bl
						add value, al
						inc count2
						inc bx
						cmp count2, 3
						jl calculateThreeInput
						
			twoInputDeclaration:
				mov tens[0], 10
				mov tens[1], 1
				jmp calculateTwoInput
					calculateTwoInput:
						mov ax, userInput[bx]
						mov bl, tens[bx]
						mul bl
						add value, al
						inc count2
						inc bx
						cmp count2, 2
						jl calculateTwoInput
						
			oneInputDeclaration:
				mov tens[0], 1
				jmp calculateOneInput
					calculateOneInput:
						mov ax, userInput[bx]
						mov bl, ones[bx]
						mul bl
						add value, al
						inc count2
						inc bx
						cmp count2, 1
						jl calculateOneInput
						
	mov dl, 0ah 							;prints new line
	mov ah, 02h
	int 21h
	
	mov bx, 7
	mov count, 0
	
	bitRepLoop:
		inc count
		mov al,value
		shr ax, 1
		mov shiftArray[bx], cl
		dec bx
	cmp count, 8
	jl bitRepLoop
	
	mov dl, value				;prints encrypted line
    mov ah, 02h
    int 21h
	
	lea dx, shiftArray				;prints encrypted line
    mov ah, 09h
    int 21h
	
	mov ax, 4c00h
	int 21h
	
	main endp
	end main