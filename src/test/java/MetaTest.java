import static org.junit.Assert.assertArrayEquals;
import hanwha.meta.Irkd;
import hanwha.meta.Meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MetaTest {
	@Parameters
	public static Collection<Object[]> cases() {
		return Arrays.asList(new Object[][] {
			{"1230", new String[]{"025","026"}},
			{"1612", new String[]{"047"}},
			{"1626", new String[]{"085","086","087","088","089","090"}}
		});
	}

	private String   보종코드_세;
	private String[] 보종코드_목_배열;

	public MetaTest(String 보종코드_세, String[] 보종코드_목_배열) {
		this.보종코드_세 = 보종코드_세;
		this.보종코드_목_배열 = 보종코드_목_배열;
	}

	@BeforeClass
	public static void setup() throws Exception {
		Class.forName(Meta.class.getName());
	}

	@Test
	public void test() {
		List<Irkd> irkdList = Meta.getIrkdList(보종코드_세);
		List<String>  items = new ArrayList<>();

		for (Irkd irkd: irkdList) {
			items.add(irkd.getIrkdCodeItem());
		}
		assertArrayEquals(보종코드_목_배열, items.toArray(new String[0]));
	}
}
