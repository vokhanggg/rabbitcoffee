package com.rabbitcoffee.rest;

import com.rabbitcoffee.common.DeviceProvider;
import com.rabbitcoffee.common.TimeProvider;
import com.rabbitcoffee.enumvalue.ERoleName;
import com.rabbitcoffee.model.Role;
import com.rabbitcoffee.model.User;
import com.rabbitcoffee.rest.AuthenticationController;
import com.rabbitcoffee.security.DeviceDummy;
import com.rabbitcoffee.security.TokenHelper;
import com.rabbitcoffee.service.impl.CustomUserDetailsService;

import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mobile.device.Device;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthenticationControllerTest {

    private MockMvc mvc;

    @MockBean
    private TimeProvider timeProviderMock;

    private static final String TEST_USERNAME = "testUser";

    @Autowired
    private TokenHelper tokenHelper;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private DeviceProvider deviceProvider;

    @Autowired
    private DeviceDummy device;

    @Before
    public void setup() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User user = new User();
        user.setUsername("username");
        Role authority = new Role();
        authority.setId(0L);
        authority.setName( ERoleName.ROLE_USER );
        List<Role> authorities = Arrays.asList(authority);
        user.setAuthorities(authorities);
        user.setLastPasswordResetDate(new Timestamp(DateUtil.yesterday().getTime()));
        when(this.userDetailsService.loadUserByUsername(eq("testUser"))).thenReturn(user);
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(tokenHelper, "EXPIRES_IN", 100); // 100 sec
        ReflectionTestUtils.setField(tokenHelper, "MOBILE_EXPIRES_IN", 200); // 200 sec
        ReflectionTestUtils.setField(tokenHelper, "SECRET", "queenvictoria");

        device.setMobile(false);
        device.setNormal(false);
        device.setTablet(false);
    }

    @Test
    public void shouldGetEmptyTokenStateWhenGivenValidOldToken() throws Exception {
        when(timeProviderMock.now())
                .thenReturn(DateUtil.yesterday());
        this.mvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer 123"))
                .andExpect(content().json("{accessToken:null,expiresIn:null}"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldRefreshNotExpiredWebToken() throws Exception {

        given(timeProviderMock.now())
                .willReturn(new Date(30L));

        device.setNormal(true);
        String token = createToken(device);
        String refreshedToken = tokenHelper.refreshToken(token, device);
        given(deviceProvider.getCurrentDevice(any()))
                .willReturn(device);
        this.mvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer " + token))
                .andExpect(content().json("{accessToken:" + refreshedToken + ",expiresIn:100}"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void shouldRefreshNotExpiredMobileToken() throws Exception {
        given(timeProviderMock.now())
                .willReturn(new Date(30L));
        device.setMobile(true);
        String token = createToken(device);
        String refreshedToken = tokenHelper.refreshToken(token, device);
        given(deviceProvider.getCurrentDevice(any()))
                .willReturn(device);
        this.mvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer " + token))
                .andExpect(content().json("{accessToken:" + refreshedToken + ",expiresIn:200}"));
    }

    @Test
    public void shouldNotRefreshExpiredWebToken() throws Exception {
        Date beforeSomeTime = new Date(DateUtil.now().getTime() - 15 * 1000);
        when(timeProviderMock.now())
                .thenReturn(beforeSomeTime);
        device.setNormal(true);
        String token = createToken(device);
        this.mvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer " + token))
                .andExpect(content().json("{accessToken:null,expiresIn:null}"));
    }

    @Test
    public void shouldRefreshExpiredMobileToken() throws Exception {
        Date beforeSomeTime = new Date(DateUtil.now().getTime() - 15 * 1000);
        when(timeProviderMock.now())
                .thenReturn(beforeSomeTime);
        device.setNormal(true);
        String token = createToken(device);
        this.mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + token))
				.andExpect(content().json("{accessToken:null,expiresIn:null}"));
    }

    private String createToken(Device device) {
        return tokenHelper.generateToken(TEST_USERNAME, device);
    }
}
