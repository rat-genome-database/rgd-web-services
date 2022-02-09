package edu.mcw.rgd.domain.vcmap;

import edu.mcw.rgd.process.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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


    public static List<MappedGeneEx> getActiveGenesInRegion(Connection conn, String chr, int startPos, int stopPos, int mapKey) throws SQLException {

        List<MappedGeneEx> results = new ArrayList<>();

        String sql = "SELECT g.rgd_id, g.gene_symbol,g.full_name gene_name,g.gene_type_lc gene_type, md.start_pos,md.stop_pos,md.strand "+
                "FROM genes g, rgd_ids r, maps_data md "+
                "WHERE r.object_status='ACTIVE' AND r.rgd_id=g.rgd_id AND md.rgd_id=g.rgd_id "+
                "AND md.chromosome=? AND md.stop_pos>=? AND md.start_pos<=? AND md.map_key=? "+
                "ORDER BY md.start_pos";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, chr);
        ps.setInt(2, startPos);
        ps.setInt(3, stopPos);
        ps.setInt(4, mapKey);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            MappedGeneEx mg = new MappedGeneEx();
            mg.geneRgdId = rs.getInt("rgd_id");
            mg.geneSymbol = rs.getString("gene_symbol");
            mg.geneName = rs.getString("full_name");
            mg.geneType = rs.getString("gene_type");

            mg.mapKey = mapKey;
            mg.chr = chr;
            mg.startPos = rs.getInt("start_pos");
            mg.stopPos = rs.getInt("stop_pos");
            mg.strand = rs.getString("strand");

            // skip rows with missing chr, start or stop pos
            if( Utils.isStringEmpty(mg.chr) || mg.startPos<=0 || mg.stopPos<=0 ) {
                continue;
            }

            results.add(mg);
        }
        return results;
    }
}
