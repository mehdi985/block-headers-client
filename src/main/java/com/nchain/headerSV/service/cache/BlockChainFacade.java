package com.nchain.headerSV.service.cache;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeaderAddr;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderAddrRepository;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.service.cache.cached.CachedBranch;
import com.nchain.headerSV.service.cache.cached.CachedHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 12/08/2020
 */
@Service
public class BlockChainFacade {

    @Autowired
    BlockHeaderCacheService blockHeaderCacheService;

    @Autowired
    BlockHeaderAddrRepository blockHeaderAddrRepository;

    private enum BranchState {
        MAIN_CHAIN,
        ORPHAN,
        STALE
    }


    public BlockHeaderQueryResult getBlockFromCache(String hash) {
        BlockHeaderQueryResult  queryResult = null;
        BlockHeader  blockHeader  = blockHeaderCacheService.getUnconnectedBlocks().get(hash);

        List<BlockHeaderAddr> blockHeaderAddrList = blockHeaderAddrRepository.findByHash(hash);

        queryResult = blockHeader != null? BlockHeaderQueryResult.builder()
               .blockHeader(blockHeader)
               .height(0)
               .work(0)
               .state(BranchState.ORPHAN.name())
               .confidence(blockHeaderAddrList.size()).build(): getBlockHeaderQueryResult(hash);

        return queryResult;
    }

    private BlockHeaderQueryResult getBlockHeaderQueryResult(String hash) {
       CachedHeader cachedHeader = blockHeaderCacheService.getConnectedBlocks().get(hash);

        CachedBranch branch = null;
        BlockHeaderQueryResult queryResult = null;
        if(cachedHeader != null) {
            branch = blockHeaderCacheService.getBranch(cachedHeader.getBranchId());


            CachedBranch  maxWorkedHoldBranch = blockHeaderCacheService.getBranches().stream().max(Comparator.comparingDouble(CachedBranch::getWork)).get();
            boolean mainBranch = false;
            String branchstate = BranchState.STALE.name();
            if (maxWorkedHoldBranch != null  && branch != null )
                if( maxWorkedHoldBranch.getWork() < branch.getWork() || (maxWorkedHoldBranch.getId().equals(branch.getId()) && maxWorkedHoldBranch.getWork() == branch.getWork())) {

                    mainBranch = true;
                    branchstate = BranchState.MAIN_CHAIN.name();
                }

            List<BlockHeaderAddr> blockHeaderAddrList = blockHeaderAddrRepository.findByHash(hash);

            queryResult = BlockHeaderQueryResult.builder()
                    .blockHeader(cachedHeader.getBlockHeader())
                    .height(cachedHeader.getHeight())
                    .work(cachedHeader.getWork())
                    .bestChain(mainBranch)
                    .state(branchstate)
                    .confidence(blockHeaderAddrList.size()).build();
        }
        return queryResult;
    }
}