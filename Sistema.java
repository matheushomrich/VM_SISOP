// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//

import java.util.*;

//import org.graalvm.compiler.word.Word;
public class Sistema {
	
	// -------------------------------------------------------------------------------------------------------
	// --------------------- H A R D W A R E - definicoes de HW ---------------------------------------------- 

	// -------------------------------------------------------------------------------------------------------
	// --------------------- M E M O R I A -  definicoes de opcode e palavra de memoria ---------------------- 
	
	public class Word { 	// cada posicao da memoria tem uma instrucao (ou um DATA)
		public Opcode opc; 	//
		public int r1; 		// indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
		public int r2; 		// indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
		public int p; 		// parametro para instrucao (k ou A cfe operacao), ou o DATA, se opcode = DATA

		public Word(Opcode _opc, int _r1, int _r2, int _p) {  
			opc = _opc;   r1 = _r1;    r2 = _r2;	p = _p;
		}
	}
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
    // --------------------- C P U  -  definicoes da CPU ----------------------------------------------------- 

	public enum Opcode {
		DATA, ___,		    // se memoria nesta posicao tem um DATA, usa DATA, se nao usada ee NULO ___
		JMP, JMPI, JMPIG, JMPIL, JMPIE,  JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,   // desvios e parada
		ADDI, SUBI,  ADD, SUB, MULT,         // matematicos
		LDI, LDD, STD,LDX, STX, SWAP, 
		TRAP;        // movimentacao
	}

	public class CPU {
							// característica do processador: contexto da CPU ...
		private int pc; 			// ... composto de program counter,
		private Word ir; 			// instruction register,
		private int[] reg;       	// registradores da CPU
		private Interrupts irpt;	// durante instrucao, interrupcao pode ser sinalizada
		private Word[] m;   // CPU acessa MEMORIA, guarda referencia 'm' a ela. memoria nao muda. ee sempre a mesma.
		private int aux;
		
		public CPU(Word[] _m) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			m = _m; 				// usa o atributo 'm' para acessar a memoria.
			reg = new int[10]; 		// aloca o espaço dos registradores
		}

		public void setContext(int _pc) {  // no futuro esta funcao vai ter que ser 
			pc = _pc;                                              // limite e pc (deve ser zero nesta versao)
		}
	
		public void run() { 		// execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
			while (true) { // ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
				// FETCH
				ir = m[pc]; 	// busca posicao da memoria apontada por pc, guarda em ir
				// EXECUTA INSTRUCAO NO ir
					switch (ir.opc) { // para cada opcode, sua execução
						case JMP: // Jump immediate
							pc = ir.p;
							break;

						case JMPI: // Jump register
							pc = ir.r1;
							break;
							
						case JMPIG: // If Rc > 0 Then PC <- Rs Else PC <- PC +1
							if (reg[ir.r2] > 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

						case JMPIL: // If Rc < 0 Then PC <- Rs Else PC <- PC +1
							if (reg[ir.r2] < 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

						case JMPIE: // If Rc == 0 Then PC <- Rs Else PC <- PC +1
							if (reg[ir.r2] == 0) {
								pc = reg[ir.r1];
							} else {
								pc++;
							}
							break;

						case JMPIM: // Jump to adress in memory
							pc = m[ir.p].p;
							break;

						case JMPIGM: // Jump to adress in memory; If Rc > 0 Then PC <- P Else PC <- PC +1
							if (reg[ir.r2] > 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case JMPILM: // Jump to adress in memory; If Rc < 0 Then PC <- P Else PC <- PC +1
							if (reg[ir.r2] < 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;
						
						case JMPIEM: // Jump to adress in memory; If Rc == 0 Then PC <- P Else PC <- PC +1
							if (reg[ir.r2] == 0) {
								pc = ir.p;
							} else {
								pc++;
							}
							break;

						case STOP: // por enquanto, para execucao
							irpt = Interrupts.intSTOP;
							System.out.println("Program ended by STOP instruction at address " + pc);
							break;

						case ADDI: // Rd <- Rd + k
							aux = reg[ir.r1];
							reg[ir.r1] = reg[ir.r1] + ir.p;
							pc++;
							if((aux > reg[ir.r1] && ir.p > 0) || (aux < reg[ir.r1] && ir.p < 0)) {
								irpt = Interrupts.intInstrucaoInvalida;
								System.out.println("Overflow Exception in register " + ir.r1);
							}
							break;

						case SUBI: // Rd <- Rd - k
							aux = reg[ir.r1];
							reg[ir.r1] = reg[ir.r1] - ir.p;
							pc++;
							if((aux < reg[ir.r1] && ir.p > 0) || (aux > reg[ir.r1] && ir.p < 0)) {
								irpt = Interrupts.intInstrucaoInvalida;
								System.out.println("Overflow Exception in register " + ir.r1);
							}
							break;

						case ADD: // Rd <- Rd + Rs
							aux = reg[ir.r1];
							reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
							pc++;
							if((aux > reg[ir.r1] && reg[ir.r2] > 0) || (aux < reg[ir.r1] && reg[ir.r2] < 0)) {
								irpt = Interrupts.intInstrucaoInvalida;
								System.out.println("Overflow Exception in register " + ir.r1);
							}
							break;

						case SUB: // Rd <- Rd - Rs
							aux = reg[ir.r1];
							reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
							pc++;
							if((aux < reg[ir.r1] && reg[ir.r2] > 0) || (aux > reg[ir.r1] && reg[ir.r2] < 0)) {
								irpt = Interrupts.intInstrucaoInvalida;
								System.out.println("Overflow Exception in register " + ir.r1);
							}
							break;

						case MULT: //Rd <- Rd * Rs
							aux = reg[ir.r1];
							reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
							pc++;
							if((aux > reg[ir.r1] && reg[ir.r2] > 0) || (aux < reg[ir.r1] && reg[ir.r2] < 0)) {
								irpt = Interrupts.intInstrucaoInvalida;
								System.out.println("Overflow Exception in register " + ir.r1);
							}
							break;

						case LDI: // Rd <- k
							reg[ir.r1] = ir.p;
							pc++;
							break;

						case LDD: // Rd <- [A]
							reg[ir.r1] = m[ir.p].p;
							pc++;
							break;

						case STD: // [A] <- Rs
							m[ir.p].opc = Opcode.DATA;
							m[ir.p].p = reg[ir.r1];
							pc++;
							break;

						case LDX: // Rd <- [Rs]
							reg[ir.r1] = m[reg[ir.r2]].p;
							pc++;
							break;

						case STX: // [Rd] <- Rs
							m[reg[ir.r1]].opc = Opcode.DATA;
							m[reg[ir.r1]].p = reg[ir.r2];
							pc++;
							break;

						case SWAP: // Rd <-> Rs
							int t = ir.r1;
							ir.r1 = ir.r2;
							ir.r2 = t;
							pc++;
							break;

						case TRAP:
							if (reg[8] == 1)
							{
								Scanner keyboard = new Scanner(System.in);
								System.out.println("Digite o numero");
								int in = keyboard.nextInt();
								m[reg[9]].opc = Opcode.DATA;
								m[reg[9]].p = in;
								//keyboard.next();
							}
							else if (reg[8] == 2)
							{
								System.out.println(m[reg[9]].p);
							}
							pc++;
							break;

							

						default:   // Instrução Inválida
						irpt = Interrupts.intEnderecoInvalido;
							System.out.println("Instrucao Invalida: " + ir.opc + " no endereco " + pc);
							break;
					
				}
				if (!(irpt == Interrupts.noInterrupt)) {
					System.out.print("Interrupcao ");
					System.out.println(irpt);
					break; // break sai do loop da cpu
				}
			}
		}
	}
    // ------------------ C P U - fim ------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	// ------------------- Iterrupts -------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------

	public enum Interrupts { // possiveis interrupcoes
		noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intSTOP;
	}


    // ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
    // -------------------------- atributos e construcao da VM -----------------------------------------------
	public class VM {
		public int tamMem;    
        public Word[] m;     
        public CPU cpu;    

        public VM(){   // vm deve ser configurada com endereço de tratamento de interrupcoes
	     // memória
  		 	 tamMem = 1024;
			 m = new Word[tamMem]; // m ee a memoria
			 for (int i=0; i<tamMem; i++) { m[i] = new Word(Opcode.___,-1,-1,-1); };
	  	 // cpu
			 cpu = new CPU(m);
	    }	
	}
    
	// ------------------- V M  - fim ------------------------------------------------------------------------
	

	
	// -------------------------------------------------------------------------------------------------------


    // --------------------H A R D W A R E - fim -------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

	// -------------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------
	// ------------------- S O F T W A R E - inicio ----------------------------------------------------------

	// ------------------- VAZIO
	

	// -------------------------------------------------------------------------------------------------------
    // -------------------  S I S T E M A --------------------------------------------------------------------

	public VM vm;

    public Sistema(){   // a VM com tratamento de interrupções
		 vm = new VM();
	}

    // -------------------  S I S T E M A - fim --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

	
    // -------------------------------------------------------------------------------------------------------
    // ------------------- instancia e testa sistema
	public static void main(String args[]) {
		Sistema s = new Sistema();
		s.test1();
	}
    // -------------------------------------------------------------------------------------------------------
    // --------------- TUDO ABAIXO DE MAIN É AUXILIAR PARA FUNCIONAMENTO DO SISTEMA - nao faz parte 

	// -------------------------------------------- teste do sistema ,  veja classe de programas
	public void test1(){
		Aux aux = new Aux();
		Word[] p = new Programas().fibonacci10;
		aux.carga(p, vm.m);
		vm.cpu.setContext(0);
		System.out.println("---------------------------------- programa carregado ");
		aux.dump(vm.m, 0, 33);
		System.out.println("---------------------------------- após execucao ");
		vm.cpu.run();
		aux.dump(vm.m, 0, 33);
	}

	public void test2(){
		Aux aux = new Aux();
		Word[] p = new Programas().progMinimo;
		aux.carga(p, vm.m);
		vm.cpu.setContext(0);
		System.out.println("---------------------------------- programa carregado ");
		aux.dump(vm.m, 0, 15);
		System.out.println("---------------------------------- após execucao ");
		vm.cpu.run();
		aux.dump(vm.m, 0, 15);
	}

	public void testp2(){
		//
		Aux aux = new Aux();
        Word[] p = new Programas().p2;
        aux.carga(p, vm.m);
        vm.cpu.setContext(0);
        System.out.println("---------------------------------- programa carregado ");
        aux.dump(vm.m, 0, 40);
        System.out.println("---------------------------------- após execucao ");
        vm.cpu.run();
        aux.dump(vm.m, 0, 40);
    }

	public void testp3(){
		//
		Aux aux = new Aux();
        Word[] p = new Programas().p3Fatorial;
        aux.carga(p, vm.m);
        vm.cpu.setContext(0);
        System.out.println("---------------------------------- programa carregado ");
        aux.dump(vm.m, 0, 40);
        System.out.println("---------------------------------- após execucao ");
        vm.cpu.run();
        aux.dump(vm.m, 0, 40);
    }

	public void testp4(){
		//
		Aux aux = new Aux();
        Word[] p = new Programas().p4BubbleSort;
        aux.carga(p, vm.m);
        vm.cpu.setContext(0);
        System.out.println("---------------------------------- programa carregado ");
        aux.dump(vm.m, 0, 40);
        System.out.println("---------------------------------- após execucao ");
        vm.cpu.run();
        aux.dump(vm.m, 0, 40);
    }

	// -------------------------------------------  classes e funcoes auxiliares
    public class Aux {
		public void dump(Word w) {
			System.out.print("[ "); 
			System.out.print(w.opc); System.out.print(", ");
			System.out.print(w.r1);  System.out.print(", ");
			System.out.print(w.r2);  System.out.print(", ");
			System.out.print(w.p);  System.out.println("  ] ");
		}
		public void dump(Word[] m, int ini, int fim) {
			for (int i = ini; i < fim; i++) {
				System.out.print(i); System.out.print(":  ");  dump(m[i]);
			}
		}
		public void carga(Word[] p, Word[] m) {
			for (int i = 0; i < p.length; i++) {
				m[i].opc = p[i].opc;     m[i].r1 = p[i].r1;     m[i].r2 = p[i].r2;     m[i].p = p[i].p;
			}
		}
   }
   // -------------------------------------------  fim classes e funcoes auxiliares
	
   //  -------------------------------------------- programas aa disposicao para copiar na memoria (vide aux.carga)
   public class Programas {
	   public Word[] progMinimo = new Word[] {
		    new Word(Opcode.LDI, 0, -1, 999), 		
			new Word(Opcode.STD, 0, -1, 10), 
			new Word(Opcode.STD, 0, -1, 11), 
			new Word(Opcode.STD, 0, -1, 12), 
			new Word(Opcode.STD, 0, -1, 13), 
			new Word(Opcode.STD, 0, -1, 14), 
			new Word(Opcode.STOP, -1, -1, -1) 
		};

	   public Word[] fibonacci10 = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
			// reg0 guarda as posicoes de memoria do programa
			new Word(Opcode.LDI, 1, -1, 0), // carrega 0 no reg1
			new Word(Opcode.STD, 1, -1, 20), // reg1 na pos 20 
			new Word(Opcode.LDI, 2, -1, 1), // carrega 1 no reg2
			new Word(Opcode.STD, 2, -1, 21), // reg2 na pos 21
			new Word(Opcode.LDI, 0, -1, 22), // carrega 22 no reg0
			new Word(Opcode.LDI, 6, -1, 6), // carrega 6 no reg6
			new Word(Opcode.LDI, 7, -1, 31), // carrega 31 no reg7
			new Word(Opcode.LDI, 3, -1, 0), // carrega 0 no reg3
			new Word(Opcode.ADD, 3, 1, -1), // soma reg3 com reg1 e guarda no reg3 -> 0 + 0 = 0
			new Word(Opcode.LDI, 1, -1, 0), // carrega 0 no reg1
			new Word(Opcode.ADD, 1, 2, -1), // soma reg1 com reg2 e guarda no reg1 -> 0 + 1 = 1
			new Word(Opcode.ADD, 2, 3, -1), // soma reg2 com reg3 e guarda no reg2 -> 1 + 0 = 1
			new Word(Opcode.STX, 0, 2, -1), // salva o conteudo de reg2 no endereço de reg0
			new Word(Opcode.ADDI, 0, -1, 1), // soma 1 no reg0, reg0 = 23 
			new Word(Opcode.SUB, 7, 0, -1), // subtrai reg7 por reg0 e guarda no reg7 -> 31 - 23 = 8
			new Word(Opcode.JMPIG, 6, 7, -1), // se o conteudo de reg7 for maior que 0 entao PC = reg6, se nao PC = PC + 1 
			new Word(Opcode.STOP, -1, -1, -1)  // PARE
		};
	
		// programa le valor e se
		public Word[] p2 = new Word[] {
			new Word(Opcode.LDI, 0, -1, 2),
			new Word(Opcode.STD, 0, -1, 30),
			new Word(Opcode.LDI, 7, -1, 35),
			new Word(Opcode.JMPILM, -1, 0, 22),
			new Word(Opcode.LDI, 1, -1, 0),
			new Word(Opcode.LDI, 2, -1, 1),
			new Word(Opcode.STX, 7, 1, -1),
			new Word(Opcode.ADDI, 7, -1, 1),
			new Word(Opcode.SUBI, 0, -1, 1),
			new Word(Opcode.JMPIGM, -1, 0, 11),
			new Word(Opcode.STOP, -1, -1, -1),
			new Word(Opcode.LDI, 3, -1, 0),
			new Word(Opcode.ADD, 3, 1, -1),
			new Word(Opcode.ADD, 3, 2, -1),
			new Word(Opcode.STX, 7, 3, -1),
			new Word(Opcode.LDI, 1, -1, 0),
			new Word(Opcode.ADD, 1, 2, -1),
			new Word(Opcode.LDI, 2, -1, 0),
			new Word(Opcode.ADD, 2, 3, -1),
			new Word(Opcode.ADDI, 7, -1, 1),
			new Word(Opcode.SUBI, 0, -1, 1),
			new Word(Opcode.JMP, -1, -1, 9),
			new Word(Opcode.LDI, 6, -1, -1),
			new Word(Opcode.STX, 7, 6, -1),
			new Word(Opcode.STOP, -1, -1, -1),


			
		}; 

		/*
		public Word[] p2 = new Word[] {
			new Word(Opcode.LDI, 4, -1, 10), // carrega 10 no reg4
			new Word(Opcode.STD, 4, -1, 60), // aloca o conteudo de 4 na posicao 60
			new Word(Opcode.LDD, 5, -1, 60), // carrega o valor da posicao 60 no reg5
			new Word(Opcode.LDI, 4, -1, 17), // carrega 17 no reg4
			new Word(Opcode.JMPIL, 4, 5, -1), // se o conteudo de de reg5 for menor que 0 entao  PC = reg4, se nao PC = PC + 1 
			// faz fibonacci
			new Word(Opcode.LDI, 1, -1, 0), // carrega 0 no reg1
			new Word(Opcode.STD, 1, -1, 20), // reg1 na pos 20 
			new Word(Opcode.LDI, 2, -1, 1), // carrega 1 no reg2
			new Word(Opcode.STD, 2, -1, 21), // reg2 na pos 21
			new Word(Opcode.LDI, 0, -1, 22), // carrega 22 no reg0
			new Word(Opcode.LDI, 6, -1, 6), // carrega 6 no reg6
			new Word(Opcode.LDI, 7, -1, 31), // carrega 31 no reg7
			new Word(Opcode.LDI, 3, -1, 0), // carrega 0 no reg3
			new Word(Opcode.ADD, 3, 1, -1), // soma reg3 com reg1 e guarda no reg3 -> 0 + 0 = 0
			new Word(Opcode.LDI, 1, -1, 0), // carrega 0 no reg1
			new Word(Opcode.ADD, 1, 2, -1), // soma reg1 com reg2 e guarda no reg1 -> 0 + 1 = 1
			new Word(Opcode.ADD, 2, 3, -1), // soma reg2 com reg3 e guarda no reg2 -> 1 + 0 = 1
			new Word(Opcode.STX, 0, 2, -1), // salva o conteudo de reg2 no endereço de reg0
			new Word(Opcode.ADDI, 0, -1, 1), // soma 1 no reg0, reg0 = 23 
			new Word(Opcode.SUB, 7, 0, -1), // subtrai reg7 por reg0 e guarda no reg7 -> 31 - 23 = 8
			new Word(Opcode.JMPIG, 6, 7, -1), // se o conteudo de reg7 for maior que 0 entao PC = reg6, se nao PC = PC + 1 
			new Word(Opcode.STOP, -1, -1, -1),  // PARE 
			// caso else
			new Word(Opcode.LDI, 4, -1, -1), // carrega -1 no reg4
			new Word(Opcode.STD, 4, -1, 65), // aloca o conteudo do reg4 na posicao 65
			new Word(Opcode.STOP, -1, -1, -1) // PARE
			
		};


		/*
		LDI 0 2
STD 0 30
LDI 7 35
JMPILM 0 22
LDI 1 0
LDI 2 1
STX 7 1
ADDI 7 1
SUBI 0 1
JMPIGM 0 11
STOP
LDI 3 0
ADD 3 1
ADD 3 2
STX 7 3
LDI 1 0
ADD 1 2
LDI 2 0
ADD 2 3
ADDI 7 1
SUBI 0 1
JMP 9
LDI 6 -1
STX 7 6
STOP
		*/

		/*
		public Word[] p2 = new Word[] {
			new Word(Opcode.LDD, 0, -1, 49),
			new Word(Opcode.LDI, 1, -1, 22), //posiçãoi para fim 1
			new Word(Opcode.JMPIL, 1, 0, -1), //comparação
			new Word(Opcode.ADDI, 0, 0, 51), //adiciona o indice de memoria no nmr de elementos para saber o fim da memoria
			new Word(Opcode.STD, 1, -1, 49),
			new Word(Opcode.LDI, 0, -1, 0),
			new Word(Opcode.STD, 0, -1, 50),
			new Word(Opcode.LDI, 1, -1, 1),
			new Word(Opcode.STD, 1, -1, 51),
			new Word(Opcode.LDI, 7, -1, 52),
			new Word(Opcode.LDI, 5, -1, 11),
			new Word(Opcode.LDD, 6, -1, 49), //pegar da posição 49 da memoria o indice do fim do array
			new Word(Opcode.LDI, 2, -1, 1),
			new Word(Opcode.ADD, 2, 0, -1),
			new Word(Opcode.LDI, 0, -1, 0),
			new Word(Opcode.ADD, 0, 1, -1),
			new Word(Opcode.LDI, 1, 2, -1),
			new Word(Opcode.STX, 7, 1, -1),
			new Word(Opcode.ADD, 7, -1, 1),
			new Word(Opcode.SUB, 6, 7, -1),
			new Word(Opcode.JMPIG, 5, 7, -1),
			new Word(Opcode.STOP, -1, -1, -1),
			new Word(Opcode.LDI, 1, -1, -1), //pular aqui
			new Word(Opcode.STD, 1, 0, 49),
			new Word(Opcode.STOP, -1, -1, -1)
	};

	*/

	// programa p3-fatorial
	public Word[] p3Fatorial = new Word[] {
		new Word(Opcode.LDI, 0, -1, 5), //alterar o valor de P para negativo ou positivo, carrega o valor 5 no reg0
		new Word(Opcode.STD, 0, -1, 30), // aloca o conteudo de reg0 na posicao 30
		new Word(Opcode.LDD, 1, -1, 30), // carrega o conteudo da posicao 30 no reg1
		new Word(Opcode.LDI, 7, -1, 11), // registrador com o valor de inicio do programa, carrega 11 no reg7
		new Word(Opcode.LDI, 6, -1, 18), // registrador com o valor de inicio do programa caso o n seja zero, carrega 18 no reg6
		new Word(Opcode.LDI, 5, -1, 21), // registrador com o valor de inicio do programa caso o n seja 1, carrega 21 no reg5
		new Word(Opcode.JMPIG, 7, 1, -1), // se o conteudo de reg1 for maior que 0 entao PC = reg7, se nao PC = PC + 1
		new Word(Opcode.JMPIE, 6, 1, -1), // se p conteudo de reg1 for igual a 0 entao PC = reg1, se nao PC = PC + 1
		new Word(Opcode.LDI, 3, -1, -1), // carrega -1 no reg3
		new Word(Opcode.STD, 3, -1, 31), // aloca o conteudo de reg3 na posicao 31
		new Word(Opcode.STOP, -1, -1, -1), // PARE

		new Word(Opcode.LDI, 4, -1, 16), //valor com o inicio do loop, carrega 16 no reg4
		new Word(Opcode.LDD, 0, -1, 30), // carrega o valor do fatorial
		new Word(Opcode.LDD, 1, -1, 30), // carrega o valor do fatorial
		new Word(Opcode.SUBI, 1, -1, 1), // diminui 1 de r1
		new Word(Opcode.JMPIE, 5, 1, -1), // 15

		new Word(Opcode.MULT,0,1,-1), // inicio do loop
		new Word(Opcode.SUBI, 1, -1, 1),
		new Word(Opcode.JMPIG, 4, 1, -1), // teste
		new Word(Opcode.STD,0,-1,31),
		new Word(Opcode.STOP, -1, -1, -1),

		new Word(Opcode.LDI, 0, -1, 1), // caso n zero
		new Word(Opcode.STD,0, -1, 31),
		new Word(Opcode.STOP, -1, -1, -1),

		new Word(Opcode.LDI, 0, -1, 1), // caso n 1
		new Word(Opcode.STD,0, -1, 31),
		new Word(Opcode.STOP, -1, -1, -1) 
	};
	
	// programa p4-BubbleSort
	public Word[] p4BubbleSort = new Word[] {
		new Word(Opcode.LDI, 0, -1, 12),  //carregando valor na memoria
		new Word(Opcode.STD, 0, -1, 40),

		new Word(Opcode.LDI, 0, -1, 20),
		new Word(Opcode.STD, 0, -1, 41),

		new Word(Opcode.LDI, 0, -1, 12),
		new Word(Opcode.STD, 0, -1, 42),

		new Word(Opcode.LDI, 0, -1, 1),
		new Word(Opcode.STD, 0, -1, 43),

		new Word(Opcode.LDI, 0, -1, 29),
		new Word(Opcode.STD, 0, -1, 44),
		
		new Word(Opcode.LDI, 0, -1, -12),
		new Word(Opcode.STD, 0, -1, 45),

		new Word(Opcode.LDI, 0, -1, 0),
		new Word(Opcode.STD, 0, -1, 46),// valores carregados

		new Word(Opcode.LDI, 3, -1, 6), 
		new Word(Opcode.LDI, 4, -1, 6), 
		new Word(Opcode.LDI, 5, -1, 20), 
		new Word(Opcode.LDI, 6, -1, 33), 
		new Word(Opcode.LDI, 7, -1, 38), 		
		new Word(Opcode.LDI, 0, -1, 40), 

		new Word(Opcode.JMPIE, 6, 3, -1), //inicio loop

		new Word(Opcode.SUBI, 3, -1, 1),  
		new Word(Opcode.LDX, 1, 0, -1),  
		new Word(Opcode.ADDI, 0, -1, 1), 
		new Word(Opcode.LDX, 2, 0, -1), 
		new Word(Opcode.SUB, 2, 1, -1), 
		new Word(Opcode.JMPIG, 5, 2, -1),

		new Word(Opcode.LDX, 2, 0, -1),
		new Word(Opcode.STX, 0, 1, -1),
		new Word(Opcode.SUBI, 0, -1, 1),
		new Word(Opcode.STX, 0, 2, -1),
		new Word(Opcode.ADDI, 0, -1, 1),
		new Word(Opcode.JMPI, 5, 0 , -1),

		new Word(Opcode.JMPIE, 7, 4, -1),
		new Word(Opcode.SUBI, 4, -1, 1),
		new Word(Opcode.LDI, 0, -1, 40),
		new Word(Opcode.LDI, 3, -1, 6),
		new Word(Opcode.JMPIG, 5, 0, -1),//fim do loop

		new Word(Opcode.STOP, -1, -1, -1)
	};

   }

}

