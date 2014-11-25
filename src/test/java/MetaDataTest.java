//import static org.junit.Assert.assertEquals;
import hanwha.meta.Good;
import hanwha.meta.Irkd;
import hanwha.meta.Meta;

import java.util.List;

import org.junit.BeforeClass;
//import org.junit.Ignore;
import org.junit.Test;

public class MetaDataTest {
	@BeforeClass
	public static void setup() throws Exception {
		Class.forName(Meta.class.getName());
	}

//	@Ignore
	@Test
	public void test() throws Exception {
		Meta.test();                    // 테스트
		Meta.print(30, 1);              // 31번째 보종부터 1보종 콘솔에 출력
		Meta.print(30, 2, "rows.txt");  // 31번째 보종부터 2보종 파일에 출력
//		Meta.print(0, 100000, "rows.txt");  // 모두 파일에 출력
	}

//	@Ignore
	@Test
	public void list() throws Exception {
		for (List<Irkd> irkdList: Meta.getIrkdListList(20140301)) {
			System.out.format("%s\n", irkdList.get(0).getIrkdCodeDtal());
			for (Irkd irkd: irkdList) {
				System.out.format("  %s-%s %s\n", irkd.getIrkdCodeDtal(),
						                          irkd.getIrkdCodeItem(),
						                          irkd.getInkdBsnsName());
//				Good g = irkd.getGoodList().get(0);
//				assertEquals(irkd.getIrkdCodeDtal(), g.getGoodCodeDtal());
//				assertEquals(irkd.getIrkdCodeItem(), g.getGoodCodeItem());
//				assertEquals(irkd.getInkdBsnsName(), g.getGoodBsnsName());
				for (Good good: irkd.getGoodList()) {
					System.out.format("    %s-%s %3d %s\n",
					                  good.getGoodCodeDtal(),
					                  good.getGoodCodeItem(),
	                                  good.getPmmiCmpnCgrt(),
					                  good.getGoodBsnsName());
				}
			}
		}
	}
}
