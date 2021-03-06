package software.amazon.logs.resourcepolicy;

import com.google.common.collect.ImmutableList;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourcePolicy;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {
    ListHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ListHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        DescribeResourcePoliciesResponse describeResourcePoliciesResponse = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ImmutableList.of(ResourcePolicy.builder().build())).build();
        final ResourceModel model = ResourceModel.builder().build();
        doReturn(describeResourcePoliciesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleNextTokenNotNull() {
        DescribeResourcePoliciesResponse describeResourcePoliciesResponse1 = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ImmutableList.of(ResourcePolicy.builder().build())).nextToken("SOME_TOKEN").build();
        DescribeResourcePoliciesResponse describeResourcePoliciesResponse2 = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ImmutableList.of(ResourcePolicy.builder().build())).build();
        final ResourceModel model = ResourceModel.builder().build();
        doReturn(describeResourcePoliciesResponse1, describeResourcePoliciesResponse2)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        verify(proxy, atLeast(2)).injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


    @Test
    public void handleRequest_InvalidParameter() {
        BaseTests.handleRequest_InvalidParameter(proxy, handler, logger, null, DescribeResourcePoliciesRequest.class);
    }

    @Test
    public void handleRequest_ServiceUnavailable() {
        BaseTests.handleRequest_ServiceUnavailable(proxy, handler, logger, null, DescribeResourcePoliciesRequest.class);
    }
}
