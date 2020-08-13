package com.nchain.headerSV.api.service;

import com.nchain.headerSV.api.exception.BlockNotFoundException;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.service.PersistenceService;
import com.nchain.headerSV.service.cache.BlockChainFacade;
import com.nchain.headerSV.service.cache.BlockHeaderQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 28/07/2020
 */
@Service
public class BlockHeaderService {

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private BlockChainFacade blockChainFacade;

    public BlockHeaderDTO getBlockHeader(String hash) throws BlockNotFoundException {
        Optional<BlockHeaderDTO> blockHeaderDTO = persistenceService.retrieveBlockHeader(hash);
        BlockHeaderDTO blockHeader  =  blockHeaderDTO.get();

        if(null == blockHeader) throw new BlockNotFoundException();

        return blockHeader;

    }

    public BlockHeaderQueryResult  getBlockStateByHash(String hash) throws BlockNotFoundException{

        BlockHeaderQueryResult block = blockChainFacade.getBlockFromCache(hash);

        if(null == block) throw new BlockNotFoundException();

        return block;

    }
}
