package edu.mcw.rgd.domain.vcmap;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import edu.mcw.rgd.dao.AbstractDAO;
import org.springframework.jdbc.object.MappingSqlQuery;

public class MappedGeneQueryEx extends MappingSqlQuery {

    public MappedGeneQueryEx(DataSource ds, String query) {
        super(ds, query);
    }

    protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        MappedGeneEx mg = new MappedGeneEx();
        mg.geneRgdId = rs.getInt("rgd_id");
        mg.geneSymbol = rs.getString("gene_symbol");
        mg.geneName = rs.getString("full_name");
        mg.geneType = rs.getString("gene_type_lc");

        mg.mapKey = rs.getInt("map_key");
        mg.chr = rs.getString("chromosome");
        mg.startPos = rs.getInt("start_pos");
        mg.stopPos = rs.getInt("stop_pos");
        mg.strand = rs.getString("strand");

        return mg;
    }

    public static List<MappedGeneEx> execute(AbstractDAO dao, String sql, Object... params) throws Exception {
        MappedGeneQueryEx q = new MappedGeneQueryEx(dao.getDataSource(), sql);
        return dao.execute(q, params);
    }
}

