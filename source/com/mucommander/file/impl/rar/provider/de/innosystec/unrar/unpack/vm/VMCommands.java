/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 31.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 * 
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unpack.vm;

/**
 * DOCUMENT ME
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class VMCommands {
	
	public static final int VM_MOV_CODE      = 0;
	public static final int VM_CMP_CODE      = 1;
	public static final int VM_ADD_CODE      = 2;
	public static final int VM_SUB_CODE      = 3;
	public static final int VM_JZ_CODE       = 4;
	public static final int VM_JNZ_CODE      = 5;
	public static final int VM_INC_CODE      = 6;
	public static final int VM_DEC_CODE      = 7;
	public static final int VM_JMP_CODE      = 8;
	public static final int VM_XOR_CODE      = 9;
	public static final int VM_AND_CODE      = 10;
	public static final int VM_OR_CODE       = 11;
	public static final int VM_TEST_CODE     = 12;
	public static final int VM_JS_CODE       = 13;
	public static final int VM_JNS_CODE      = 14;
	public static final int VM_JB_CODE       = 15;
	public static final int VM_JBE_CODE      = 16;
	public static final int VM_JA_CODE       = 17;
	public static final int VM_JAE_CODE      = 18;
	public static final int VM_PUSH_CODE     = 19;
	public static final int VM_POP_CODE      = 20;
	public static final int VM_CALL_CODE     = 21;
	public static final int VM_RET_CODE      = 22;
	public static final int VM_NOT_CODE      = 23;
	public static final int VM_SHL_CODE      = 24;
	public static final int VM_SHR_CODE      = 25;
	public static final int VM_SAR_CODE      = 26;
	public static final int VM_NEG_CODE      = 27;
	public static final int VM_PUSHA_CODE    = 28;
	public static final int VM_POPA_CODE     = 29;
	public static final int VM_PUSHF_CODE    = 30;
	public static final int VM_POPF_CODE     = 31;
	public static final int VM_MOVZX_CODE    = 32;
	public static final int VM_MOVSX_CODE    = 33;
	public static final int VM_XCHG_CODE     = 34;
	public static final int VM_MUL_CODE      = 35;
	public static final int VM_DIV_CODE      = 36;
	public static final int VM_ADC_CODE      = 37;
	public static final int VM_SBB_CODE      = 38;
	public static final int VM_PRINT_CODE    = 39;
	public static final int VM_MOVB_CODE     = 40;
	public static final int VM_MOVD_CODE     = 41;
	public static final int VM_CMPB_CODE     = 42;
	public static final int VM_CMPD_CODE     = 43;
	public static final int VM_ADDB_CODE     = 44;
	public static final int VM_ADDD_CODE     = 45;
	public static final int VM_SUBB_CODE     = 46;
	public static final int VM_SUBD_CODE     = 47;
	public static final int VM_INCB_CODE     = 48;
	public static final int VM_INCD_CODE     = 49;
	public static final int VM_DECB_CODE     = 50;
	public static final int VM_DECD_CODE     = 51;
	public static final int VM_NEGB_CODE     = 52;
	public static final int VM_NEGD_CODE     = 53;
	public static final int VM_STANDARD_CODE = 54;
		
	public static final VMCommands VM_MOV = new VMCommands(VM_MOV_CODE);
	public static final VMCommands VM_CMP = new VMCommands(VM_CMP_CODE);
	public static final VMCommands VM_ADD = new VMCommands(VM_ADD_CODE);
	public static final VMCommands VM_SUB = new VMCommands(VM_SUB_CODE);
	public static final VMCommands VM_JZ = new VMCommands(VM_JZ_CODE);
	public static final VMCommands VM_JNZ = new VMCommands(VM_JNZ_CODE);
	public static final VMCommands VM_INC = new VMCommands(VM_INC_CODE);
	public static final VMCommands VM_DEC = new VMCommands(VM_DEC_CODE);
	public static final VMCommands VM_JMP = new VMCommands(VM_JMP_CODE);
	public static final VMCommands VM_XOR = new VMCommands(VM_XOR_CODE);
	public static final VMCommands VM_AND = new VMCommands(VM_AND_CODE);
	public static final VMCommands VM_OR = new VMCommands(VM_OR_CODE);
	public static final VMCommands VM_TEST = new VMCommands(VM_TEST_CODE);
	public static final VMCommands VM_JS = new VMCommands(VM_JS_CODE);
	public static final VMCommands VM_JNS = new VMCommands(VM_JNS_CODE);
	public static final VMCommands VM_JB = new VMCommands(VM_JB_CODE);
	public static final VMCommands VM_JBE = new VMCommands(VM_JBE_CODE);
	public static final VMCommands VM_JA = new VMCommands(VM_JA_CODE);
	public static final VMCommands VM_JAE = new VMCommands(VM_JAE_CODE);
	public static final VMCommands VM_PUSH = new VMCommands(VM_PUSH_CODE);
	public static final VMCommands VM_POP = new VMCommands(VM_POP_CODE);
	public static final VMCommands VM_CALL = new VMCommands(VM_CALL_CODE);
	public static final VMCommands VM_RET = new VMCommands(VM_RET_CODE);
	public static final VMCommands VM_NOT = new VMCommands(VM_NOT_CODE);
	public static final VMCommands VM_SHL = new VMCommands(VM_SHL_CODE);
	public static final VMCommands VM_SHR = new VMCommands(VM_SHR_CODE);
	public static final VMCommands VM_SAR = new VMCommands(VM_SAR_CODE);
	public static final VMCommands VM_NEG = new VMCommands(VM_NEG_CODE);
	public static final VMCommands VM_PUSHA = new VMCommands(VM_PUSHA_CODE);
	public static final VMCommands VM_POPA = new VMCommands(VM_POPA_CODE);
	public static final VMCommands VM_PUSHF = new VMCommands(VM_PUSHF_CODE);
	public static final VMCommands VM_POPF = new VMCommands(VM_POPF_CODE);
	public static final VMCommands VM_MOVZX = new VMCommands(VM_MOVZX_CODE);
	public static final VMCommands VM_MOVSX = new VMCommands(VM_MOVSX_CODE);
	public static final VMCommands VM_XCHG = new VMCommands(VM_XCHG_CODE);
	public static final VMCommands VM_MUL = new VMCommands(VM_MUL_CODE);
	public static final VMCommands VM_DIV = new VMCommands(VM_DIV_CODE);
	public static final VMCommands VM_ADC = new VMCommands(VM_ADC_CODE);
	public static final VMCommands VM_SBB = new VMCommands(VM_SBB_CODE);
	public static final VMCommands VM_PRINT = new VMCommands(VM_PRINT_CODE);
	public static final VMCommands VM_MOVB = new VMCommands(VM_MOVB_CODE);
	public static final VMCommands VM_MOVD = new VMCommands(VM_MOVD_CODE);
	public static final VMCommands VM_CMPB = new VMCommands(VM_CMPB_CODE);
	public static final VMCommands VM_CMPD = new VMCommands(VM_CMPD_CODE);
	public static final VMCommands VM_ADDB = new VMCommands(VM_ADDB_CODE);
	public static final VMCommands VM_ADDD = new VMCommands(VM_ADDD_CODE);
	public static final VMCommands VM_SUBB = new VMCommands(VM_SUBB_CODE);
	public static final VMCommands VM_SUBD = new VMCommands(VM_SUBD_CODE);
	public static final VMCommands VM_INCB = new VMCommands(VM_INCB_CODE);
	public static final VMCommands VM_INCD = new VMCommands(VM_INCD_CODE);
	public static final VMCommands VM_DECB = new VMCommands(VM_DECB_CODE);
	public static final VMCommands VM_DECD = new VMCommands(VM_DECD_CODE);
	public static final VMCommands VM_NEGB = new VMCommands(VM_NEGB_CODE);
	public static final VMCommands VM_NEGD = new VMCommands(VM_NEGD_CODE);
	public static final VMCommands VM_STANDARD = new VMCommands(VM_STANDARD_CODE);

	private int m_vmCommand;

	private VMCommands(int VMCommands) {
		m_vmCommand = VMCommands;
	}

	public int getVMCommand() {
		return m_vmCommand;
	}

	public boolean equals(int VMCommands) {
		return m_vmCommand == VMCommands;
	}

	public static VMCommands findVMCommand(int VMCommands) {
		if (VM_MOV_CODE == VMCommands) {
			return VM_MOV;
		}
		if (VM_CMP_CODE == VMCommands) {
			return VM_CMP;
		}
		if (VM_ADD_CODE == VMCommands) {
			return VM_ADD;
		}
		if (VM_SUB_CODE == VMCommands) {
			return VM_SUB;
		}
		if (VM_JZ_CODE == VMCommands) {
			return VM_JZ;
		}
		if (VM_JNZ_CODE == VMCommands) {
			return VM_JNZ;
		}
		if (VM_INC_CODE == VMCommands) {
			return VM_INC;
		}
		if (VM_DEC_CODE == VMCommands) {
			return VM_DEC;
		}
		if (VM_JMP_CODE == VMCommands) {
			return VM_JMP;
		}
		if (VM_XOR_CODE == VMCommands) {
			return VM_XOR;
		}
		if (VM_AND_CODE == VMCommands) {
			return VM_AND;
		}
		if (VM_OR_CODE == VMCommands) {
			return VM_OR;
		}
		if (VM_TEST_CODE == VMCommands) {
			return VM_TEST;
		}
		if (VM_JS_CODE == VMCommands) {
			return VM_JS;
		}
		if (VM_JNS_CODE == VMCommands) {
			return VM_JNS;
		}
		if (VM_JB_CODE == VMCommands) {
			return VM_JB;
		}
		if (VM_JBE_CODE == VMCommands) {
			return VM_JBE;
		}
		if (VM_JA_CODE == VMCommands) {
			return VM_JA;
		}
		if (VM_JAE_CODE == VMCommands) {
			return VM_JAE;
		}
		if (VM_PUSH_CODE == VMCommands) {
			return VM_PUSH;
		}
		if (VM_POP_CODE == VMCommands) {
			return VM_POP;
		}
		if (VM_CALL_CODE == VMCommands) {
			return VM_CALL;
		}
		if (VM_RET_CODE == VMCommands) {
			return VM_RET;
		}
		if (VM_NOT_CODE == VMCommands) {
			return VM_NOT;
		}
		if (VM_SHL_CODE == VMCommands) {
			return VM_SHL;
		}
		if (VM_SHR_CODE == VMCommands) {
			return VM_SHR;
		}
		if (VM_SAR_CODE == VMCommands) {
			return VM_SAR;
		}
		if (VM_NEG_CODE == VMCommands) {
			return VM_NEG;
		}
		if (VM_PUSHA_CODE == VMCommands) {
			return VM_PUSHA;
		}
		if (VM_POPA_CODE == VMCommands) {
			return VM_POPA;
		}
		if (VM_PUSHF_CODE == VMCommands) {
			return VM_PUSHF;
		}
		if (VM_POPF_CODE == VMCommands) {
			return VM_POPF;
		}
		if (VM_MOVZX_CODE == VMCommands) {
			return VM_MOVZX;
		}
		if (VM_MOVSX_CODE == VMCommands) {
			return VM_MOVSX;
		}
		if (VM_XCHG_CODE == VMCommands) {
			return VM_XCHG;
		}
		if (VM_MUL_CODE == VMCommands) {
			return VM_MUL;
		}
		if (VM_DIV_CODE == VMCommands) {
			return VM_DIV;
		}
		if (VM_ADC_CODE == VMCommands) {
			return VM_ADC;
		}
		if (VM_SBB_CODE == VMCommands) {
			return VM_SBB;
		}
		if (VM_PRINT_CODE == VMCommands) {
			return VM_PRINT;
		}
		if (VM_MOVB_CODE == VMCommands) {
			return VM_MOVB;
		}
		if (VM_MOVD_CODE == VMCommands) {
			return VM_MOVD;
		}
		if (VM_CMPB_CODE == VMCommands) {
			return VM_CMPB;
		}
		if (VM_CMPD_CODE == VMCommands) {
			return VM_CMPD;
		}
		if (VM_ADDB_CODE == VMCommands) {
			return VM_ADDB;
		}
		if (VM_ADDD_CODE == VMCommands) {
			return VM_ADDD;
		}
		if (VM_SUBB_CODE == VMCommands) {
			return VM_SUBB;
		}
		if (VM_SUBD_CODE == VMCommands) {
			return VM_SUBD;
		}
		if (VM_INCB_CODE == VMCommands) {
			return VM_INCB;
		}
		if (VM_INCD_CODE == VMCommands) {
			return VM_INCD;
		}
		if (VM_DECB_CODE == VMCommands) {
			return VM_DECB;
		}
		if (VM_DECD_CODE == VMCommands) {
			return VM_DECD;
		}
		if (VM_NEGB_CODE == VMCommands) {
			return VM_NEGB;
		}
		if (VM_NEGD_CODE == VMCommands) {
			return VM_NEGD;
		}
		if (VM_STANDARD_CODE == VMCommands) {
			return VM_STANDARD;
		}
		return null;
	}
}
