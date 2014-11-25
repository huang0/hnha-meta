package hanwha.meta;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
class Dao {
	private static final String QUERY =
		"SELECT A.IRKD_CODE_DTAL||A.IRKD_CODE_ITEM 보험종류코드\n"+
		"     , A.INKD_BSNS_NAME                   보험종류업무명\n"+
		"     , C.GOOD_CODE_DTAL||C.GOOD_CODE_ITEM 상품코드\n"+
		"     , C.GOOD_BSNS_NAME                   상품업무명\n"+
		"     , C.PMMI_CMPN_CGRT                   실손회사부담율\n"+
		"     , B.SALE_STRT_DATE                   판매시작일\n"+
		"     , B.SALE_END_DATE                    판매종료일\n"+
		"  FROM TB_MMNORSUB A\n"+
		"     , TB_MMNORCON B\n"+
		"     , TB_MMNORMAT C\n"+
		" WHERE A.IRKD_CODE_DTAL = B.IRKD_CODE_DTAL\n"+
		"   AND A.IRKD_CODE_ITEM = B.IRKD_CODE_ITEM\n"+
		"   AND B.SALE_CHNL_CODE = '1' -- 판매채널코드 (1=개인)\n"+
		"   AND B.GOOD_CODE_DTAL = C.GOOD_CODE_DTAL\n"+
		"   AND B.GOOD_CODE_ITEM = C.GOOD_CODE_ITEM\n"+
		"   AND A.IRKD_CODE_ITEM <> '000'\n"+
		" ORDER BY 보험종류코드, 상품코드\n";

	static List<Row> getRowList() throws Exception {
		try (Closeable c = new AnnotationConfigApplicationContext(Config.class)) {
			return jdbcTemplate.query(QUERY, new RowMapper<Row>() {
				@Override
				public Row mapRow(ResultSet rs, int rowNum) throws SQLException {
					Row row      = new Row();
					row.irkdCode = rs.getInt("보험종류코드");
					row.irkdName = rs.getString("보험종류업무명");
					row.goodCode = rs.getInt("상품코드");
					row.goodName = rs.getString("상품업무명");
					row.goodRate = rs.getByte("실손회사부담율");
					row.strtDate = rs.getInt("판매시작일");
					row.endDate  = rs.getInt("판매종료일");
					return row;
				}
			});
		}
	}

	@Autowired
	private void setJdbc(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static JdbcTemplate jdbcTemplate;
}
