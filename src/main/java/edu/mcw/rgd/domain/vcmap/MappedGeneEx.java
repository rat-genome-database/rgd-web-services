package edu.mcw.rgd.domain.vcmap;

public class MappedGeneEx {

    public int geneRgdId;
    public String geneSymbol;
    public String geneName;
    public String geneType;

    public int mapKey;
    public String chr;
    public int startPos;
    public int stopPos;
    public String strand;

    public String toString() {
        return "RGD:"+geneRgdId+" "+geneSymbol+" "+geneType+" MAP_KEY:"+mapKey+" c"+chr+":"+startPos+".."+stopPos+" "+strand;
    }
}
