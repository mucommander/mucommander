package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception;

public class RarExceptionType {
	private static final String notImplementedYetCode     = "Not implemented yet";
	private static final String crcErrorCode              = "CRC error";
	private static final String notRarArchiveCode         = "Not rar archive";
	private static final String badRarArchiveCode         = "Corrupted rar archive";
	private static final String unkownErrorCode           = "Unknown error";
	private static final String headerNotInArchiveCode    = "Header not in archive";
	private static final String wrongHeaderTypeCode       = "Wrong header type";
	private static final String ioErrorCode               = "IO error";
	private static final String rarEncryptedExceptionCode = "Rar encrypted exception";
	private static final String mvNotImplementedCode      = "Multi-volume not implemented yet";
	private static final String buttomlessArchiveCode     = "Unexpected end of archive";
	
	public static final RarExceptionType notImplementedYet     = new RarExceptionType(notImplementedYetCode);
	public static final RarExceptionType crcError              = new RarExceptionType(crcErrorCode);
	public static final RarExceptionType notRarArchive         = new RarExceptionType(notRarArchiveCode);
	public static final RarExceptionType badRarArchive         = new RarExceptionType(badRarArchiveCode);
	public static final RarExceptionType unkownError           = new RarExceptionType(unkownErrorCode);
	public static final RarExceptionType headerNotInArchive    = new RarExceptionType(headerNotInArchiveCode);
	public static final RarExceptionType wrongHeaderType       = new RarExceptionType(wrongHeaderTypeCode);
	public static final RarExceptionType ioError               = new RarExceptionType(ioErrorCode);
	public static final RarExceptionType rarEncryptedException = new RarExceptionType(rarEncryptedExceptionCode);
	public static final RarExceptionType mvNotImplemented	   = new RarExceptionType(mvNotImplementedCode);
	public static final RarExceptionType buttomlessArchive	   = new RarExceptionType(buttomlessArchiveCode);
	
	private static final RarExceptionType[] exceptions = {notImplementedYet, crcError, notRarArchive,
		badRarArchive, unkownError, headerNotInArchive, wrongHeaderType, ioError, rarEncryptedException};	
	
	private String name;
	
	private RarExceptionType(String name) { this.name = name; }
    
    public boolean equals(Object obj) {
    	if (obj instanceof RarExceptionType)
    		return name.equals(((RarExceptionType) obj).name);
    	return false;
    }
    
    public String name() { return name; }
}
