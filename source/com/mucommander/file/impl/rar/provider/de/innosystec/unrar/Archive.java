/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression
 * algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;"
 */
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception.RarException;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.exception.RarExceptionType;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.AVHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.BaseBlock;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.BlockHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.CommentHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.EAHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.EndArcHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.MacInfoHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.MainHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.MarkHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.ProtectHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.SignHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.SubBlockHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.SubBlockHeaderType;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.UnixOwnersHeader;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.UnrarHeadertype;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unpack.ComprDataIO;
import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unpack.Unpack;
import com.mucommander.io.BufferPool;

/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class Archive {
    
    private static Logger logger = Logger.getLogger(Archive.class.getName());

    private final ComprDataIO dataIO;

    private final List headers = new ArrayList();
    
    /** Contains FileHeader instances corresponding to the archive's entries, in the order they were found in the archive. */
    private final Vector entries = new Vector(); // FileHeaders

    /** Maps entry names to corresponding FileHeader instances */
    private Hashtable nameMap = new Hashtable();
    
    /** Maps entry names to corresponding FileHeader indexes */
    private Hashtable indexMap = new Hashtable();
    
    private MarkHeader markHead = null;

    private MainHeader newMhd = null;

//  private final UnrarCallback unrarCallback;
//    private EndArcHeader endHeader = null;
//    private Unpack unpack;
    /** Archive data CRC. */
//    private long arcDataCRC = 0xffffffff;
//    private int currentHeaderIndex;
//    private boolean encrypted = false;
//    private int sfxSize = 0;
    /** Size of packed data in current file. */
//    private long totalPackedSize = 0L;
    /** Number of bytes of compressed data read from current file. */
//    private long totalPackedRead = 0L;
 
    /**
     * create a new archive object using the given file
     * @param file the file to extract
     * @throws RarException
     */
    public Archive(InputStream in) throws RarException, IOException {
    	parseHeaders(in);
    	
        dataIO = new ComprDataIO(this);
    }

    /**
     *
     * @return whether the archive is encrypted
     */
    public boolean isEncrypted() {
        if(newMhd!=null){
            return newMhd.isEncrypted();
        }else{
            throw new NullPointerException("mainheader is null");
        }
    }
    
    private byte[] realloc(byte[] array, int size) {
    	BufferPool.releaseByteArray(array);
    	return BufferPool.getByteArray(size);
    }
    
    /**
     * Read the headers of the archive
     * @throws RarException
     */
    private void readHeaders(InputStream in) throws IOException, RarException{
        markHead = null;
        newMhd = null;
//        endHeader = null;
        headers.clear();
//        currentHeaderIndex = 0;
        long position = 0;
        long read = 0;
        byte[] baseBlockBuffer = new byte[BaseBlock.BaseBlockSize];
        byte[] buff = BufferPool.getByteArray();
        BaseBlock block;

        while((read = in.read(baseBlockBuffer)) != 0) {
//            logger.info("\n--------reading header--------");
            block = new BaseBlock(baseBlockBuffer);            
            block.setPositionInFile(position);
            position += read;
            
            logger.info("block.getHeaderType() = " + block.getHeaderType());
            switch(block.getHeaderType()) {
                
                case UnrarHeadertype.MarkHeaderCode:
    				logger.info("--MarkHeader--");
                    markHead = new MarkHeader(block);
                    if (!markHead.isSignature()) {
                        throw new RarException(RarExceptionType.badRarArchive);
                    }
                    headers.add(markHead);
//                    markHead.print();
                    break;
                    
                case UnrarHeadertype.MainHeaderCode:
                	logger.info("--MainHeader--");
                	buff = realloc(buff, block.hasEncryptVersion() ? MainHeader.mainHeaderSizeWithEnc : MainHeader.mainHeaderSize);
                	//buff = new byte[block.hasEncryptVersion() ? MainHeader.mainHeaderSizeWithEnc : MainHeader.mainHeaderSize];
                	position += in.read(buff);
                    MainHeader mainhead =new MainHeader(block, buff);
                    headers.add(mainhead);
                    this.newMhd = mainhead;
                    if(newMhd.isEncrypted()){
                        throw new RarException(
                                RarExceptionType.rarEncryptedException);
                    }
//                    mainhead.print();
                    break;
                    
                case UnrarHeadertype.SignHeaderCode:
                	logger.info("--SignHeader--");
                	buff = realloc(buff, SignHeader.signHeaderSize);
                	//buff = new byte[SignHeader.signHeaderSize];
    				position += in.read(buff);
    				headers.add(new SignHeader(block, buff));
//                    logger.info("HeaderType: SignHeader");
                    break;
                    
                case UnrarHeadertype.AvHeaderCode:
                	logger.info("--AvHeader--");
//                	buff = new byte[AVHeader.avHeaderSize];
                	buff = realloc(buff, AVHeader.avHeaderSize);
    				position += in.read(buff);
    				headers.add(new AVHeader(block, buff));
                    break;
                    
                case UnrarHeadertype.CommHeaderCode:
                	logger.info("--CommHeader--");
//                	buff = new byte[CommentHeader.commentHeaderSize];
                	buff = realloc(buff, CommentHeader.commentHeaderSize);
    				position += in.read(buff);
    				CommentHeader header = new CommentHeader(block, buff);
    				headers.add(header);
    				position += in.skip(header.getHeaderSize());
                    break;
                    
                case UnrarHeadertype.EndArcHeaderCode:
                	logger.info("--EndArcHeader--");
    				int toRead = 0;
    				toRead += block.hasArchiveDataCRC() ? EndArcHeader.endArcArchiveDataCrcSize : 0;
    				toRead += block.hasVolumeNumber() ? EndArcHeader.endArcVolumeNumberSize : 0;
    				EndArcHeader endArcHead;
    				if (toRead > 0) {
//    				    buff = new byte[toRead];
    					buff = realloc(buff, toRead);
    				    position += in.read(buff);
    				    endArcHead = new EndArcHeader(block, buff);
    				    logger.info("--endArc with data--");
    				}
    				else {
    				    endArcHead = new EndArcHeader(block, null);
    				    logger.info("--endArc without data--");
    				}
    				headers.add(endArcHead);
    				return;
                    
                default:
//                	buff = new byte[BlockHeader.blockHeaderSize];
                	buff = realloc(buff, BlockHeader.blockHeaderSize);
                	position += in.read(buff);
                	BlockHeader blockHead = new BlockHeader(block, buff);
                    
                	logger.info("blockHead.getHeaderType() = " + blockHead.getHeaderType());
                    switch(blockHead.getHeaderType()) {
                        case UnrarHeadertype.NewSubHeaderCode:
                        case UnrarHeadertype.FileHeaderCode:
                        	logger.info("--NewSubHeader/FileHeader--");
//                        	buff = new byte[blockHead.getHeaderSize() - BlockHeader.blockHeaderSize - BaseBlock.BaseBlockSize];
                        	buff = realloc(buff, blockHead.getHeaderSize() - BlockHeader.blockHeaderSize - BaseBlock.BaseBlockSize);
        				    position += in.read(buff);
        				    FileHeader fh = new FileHeader(blockHead, buff);
        				    headers.add(fh);
        				    position += in.skip(fh.getPositionInFile() + fh.getHeaderSize() + fh.getFullPackSize() - position);
                            break;
                            
                        case UnrarHeadertype.ProtectHeaderCode:
                        	logger.info("--ProtectHeader--");
//                        	buff = new byte[blockHead.getHeaderSize() - BaseBlock.BaseBlockSize - BlockHeader.blockHeaderSize];
                        	buff= realloc(buff, blockHead.getHeaderSize() - BaseBlock.BaseBlockSize - BlockHeader.blockHeaderSize);
        				    position += in.read(buff);
        				    ProtectHeader ph = new ProtectHeader(blockHead, buff);
        				    position += in.skip(ph.getHeaderSize());
                            break;
                            
                        case UnrarHeadertype.SubHeaderCode:
						{
							logger.info("--SubHeader--");
//							buff = new byte[SubBlockHeader.subBlockHeaderSize];
							buff = realloc(buff, SubBlockHeader.subBlockHeaderSize);
							position += in.read(buff);
							SubBlockHeader sh = new SubBlockHeader(blockHead, buff);
//							sh.print();
							switch (sh.getSubType().getSubblocktype()) {
								case SubBlockHeaderType.MAC_HEAD_CODE:
								{
//									buff = new byte[MacInfoHeader.MacInfoHeaderSize];
									buff= realloc(buff, MacInfoHeader.MacInfoHeaderSize);
									position += in.read(buff);
									MacInfoHeader macHeader = new MacInfoHeader(sh, buff);
//									macHeader.print();
									headers.add(macHeader);									
									break;
								}
									//TODO implement other subheaders
								case SubBlockHeaderType.BEEA_HEAD_CODE:
									break;
								case SubBlockHeaderType.EA_HEAD_CODE:
								{
//									buff = new byte[EAHeader.EAHeaderSize];
									buff= realloc(buff, EAHeader.EAHeaderSize);
									position += in.read(buff);
									EAHeader eaHeader = new EAHeader(sh, buff);
//									eaHeader.print();
									headers.add(eaHeader);									
									break;
								}
								case SubBlockHeaderType.NTACL_HEAD_CODE:
									break;
								case SubBlockHeaderType.STREAM_HEAD_CODE:
									break;
								case SubBlockHeaderType.UO_HEAD_CODE:
									toRead = sh.getHeaderSize();
									toRead -= BaseBlock.BaseBlockSize;
									toRead -= BlockHeader.blockHeaderSize;
									toRead -= SubBlockHeader.subBlockHeaderSize;
//									buff = new byte[toRead];
									buff = realloc(buff, toRead);
									position += in.read(buff);
									UnixOwnersHeader uoHeader = new UnixOwnersHeader(sh, buff);
//									uoHeader.print();
									headers.add(uoHeader);
									break;
								default:
									break;
							}
		
							break;
						}
                        default:
                            logger.warning("Unknown Header " + blockHead.getHeaderType());
                            throw new RarException(RarExceptionType.notRarArchive);
                            
                    }
                    
                    BufferPool.releaseByteArray(buff);
            }
//            logger.info("\n--------end header--------");
        }
    }
    
    private void parseHeaders(InputStream in) throws IOException {
    	try {
    		readHeaders(in);
    	} catch (RarException e) {
    		throw new IOException("exception in archive constructor maybe file is encrypted or currupt");
    	}
    	
    	entries.clear();
    	nameMap.clear();
    	indexMap.clear();

    	Iterator headersIterator = headers.iterator();
    	int index = 0;
    	BaseBlock block;
    	while (headersIterator.hasNext())
    		if ((block = (BaseBlock) headersIterator.next()).getHeaderType() == UnrarHeadertype.FileHeaderCode) {
    			FileHeader header = (FileHeader) block;
    			entries.add(header);
    			nameMap.put(header.getFileNameString(), header);
    			indexMap.put(header.getFileNameString(), Integer.valueOf(index++));
    		}
    }
    
    /**
	 * @return returns all file headers (entries) of the archive
	 */
	public List getFileHeaders() throws IOException {
		return entries;
	}
	
	public FileHeader getFileHeader(String path) throws IOException {
		FileHeader header = (FileHeader) nameMap.get(path);
		if (header == null)
			throw new IOException("ERROR: entry \"" + path + "\" does not exist in archive file");
		else
			return header;
	}
	
	public void extractEntry(boolean isSolid, FileHeader hd, InputStream in1, OutputStream os, InputStream in2) throws Exception {
		if (isSolid) {
			int headerIndex = ((Integer) indexMap.get(hd.getFileNameString())).intValue();        	
        	FileHeader entry = null;
        	for (int i = headerIndex - 1; i >= 0 ; --i)
        		if (!(entry = (FileHeader) entries.get(i)).isSolid())
        			break;
        	extractSolidEntry(entry, hd, os, in1, in2);
		}
		else {
			extractNotSolidEntry(hd, os, in1);
		}
	}

	private void extractSolidEntry(FileHeader prev, FileHeader hd, OutputStream os, InputStream in1, InputStream in2) throws Exception{
		dataIO.init(null);
        dataIO.init(prev, in1);
        dataIO.setUnpFileCRC(this.isOldFormat()?0:0xffFFffFF);
        Unpack unpack = new Unpack(dataIO);
        unpack.init(null);        
        unpack.setDestSize(prev.getFullUnpackSize());
        unpack.doUnpack(prev.getUnpVersion(), prev.isSolid());
       	//dataIO.setTestMode(false);
            // Verify file CRC
            /*prev = dataIO.getSubHeader();
            long actualCRC1 = prev.isSplitAfter() ?
                    ~dataIO.getPackedCRC() : ~dataIO.getUnpFileCRC();
            int expectedCRC1 = prev.getFileCRC();
            
            if(actualCRC1 != expectedCRC1){            	
                throw new RarException(RarExceptionType.crcError);
            }*/ 
       	unpack.cleanUp();
            
            
        dataIO.init(os);
        dataIO.init(hd, in2);
        dataIO.setUnpFileCRC(this.isOldFormat()?0:0xffFFffFF);
        unpack.setDestSize(hd.getFullUnpackSize());
        //try {
            unpack.doUnpack(hd.getUnpVersion(), hd.isSolid());
            // Verify file CRC
            hd = dataIO.getSubHeader();
            long actualCRC = hd.isSplitAfter() ?
                    ~dataIO.getPackedCRC() : ~dataIO.getUnpFileCRC();
            long expectedCRC = hd.getFileCRC();
            unpack.cleanUp();
            if(actualCRC != expectedCRC){            	
                throw new RarException(RarExceptionType.crcError);
            }
    }
	
    /**
     * Extract the file specified by the given header and write it
     * to the supplied output stream
     *
     * @param header the header to be extracted
     * @param os the outputstream
     * @throws RarException
     */
    private void extractNotSolidEntry(FileHeader hd, OutputStream os, InputStream in) throws Exception{
    	dataIO.init(os);
        dataIO.init(hd, in);
        dataIO.setUnpFileCRC(this.isOldFormat()?0:0xffFFffFF);
        Unpack unpack = new Unpack(dataIO);
        if(!hd.isSolid()){
            unpack.init(null);
        }
        unpack.setDestSize(hd.getFullUnpackSize());
        unpack.doUnpack(hd.getUnpVersion(), hd.isSolid());
        // Verify file CRC
        hd = dataIO.getSubHeader();
        long actualCRC = hd.isSplitAfter() ? ~dataIO.getPackedCRC() : ~dataIO.getUnpFileCRC();
        int expectedCRC = hd.getFileCRC();
        unpack.cleanUp();
        if(actualCRC != expectedCRC){            	
        	throw new RarException(RarExceptionType.crcError);
        }
    }
    
    /**
     * @return returns the main header of this archive
     */
    public MainHeader getMainHeader() {
        return newMhd;
    }
    
    /**
     * @return whether the archive is old format
     */
    public boolean isOldFormat() {
        return markHead.isOldFormat();
    }
}
