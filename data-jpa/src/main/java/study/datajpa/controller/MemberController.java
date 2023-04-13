package study.datajpa.controller;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberRepository memberRepository;

	@GetMapping("/members/{id}")
	public String findMember(@PathVariable("id") Long id) {
		Member member = memberRepository.findById(id).get();
		return member.getUsername();
	}

	// 도메인 컨버터 클래스 활용, 사용 권장 X
	@GetMapping("/members2/{id}")
	public String findMember2(@PathVariable("id") Member member) {
		return member.getUsername();
	}

	// 쿼리파라미터로 page, size를 사용하면 자동으로 Pageable의 구현체를 생성하여 바인딩해준다.
	// page=0, size=20이 기본값이다.
	@GetMapping("/members")
	public Page<MemberDto> list(Pageable pageable) {
		Page<Member> page = memberRepository.findAll(pageable);
		return page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
	}

	// 개별적으로 기본값 변경 가능
	@GetMapping("/members2")
	public Page<MemberDto> list2(@PageableDefault(size = 5, sort = "username", direction = Direction.DESC) Pageable pageable) {
		Page<Member> page = memberRepository.findAll(pageable);
		return page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
	}

	@PostConstruct
	public void init() {
		for (int i = 0; i < 100; i++) {
			memberRepository.save(new Member("member" + (i + 1)));
		}
	}
}
