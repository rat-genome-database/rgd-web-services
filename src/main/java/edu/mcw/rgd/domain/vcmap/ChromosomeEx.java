package edu.mcw.rgd.domain.vcmap;

import edu.mcw.rgd.datamodel.Chromosome;

public class ChromosomeEx extends Chromosome {

    public int ordinalNumber;

    public ChromosomeEx(Chromosome c) {
        setChromosome(c.getChromosome());
        setContigCount(c.getContigCount());
        setGapCount(c.getGapCount());
        setGapLength(c.getGapLength());
        setGenbankId(c.getGenbankId());
        setMapKey(c.getMapKey());
        setRefseqId(c.getRefseqId());
        setSeqLength(c.getSeqLength());

        ordinalNumber = getOrdinalNumber();
    }
}
