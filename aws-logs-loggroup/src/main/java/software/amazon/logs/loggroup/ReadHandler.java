package software.amazon.logs.loggroup;

import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        if (model == null || model.getLogGroupName() == null) {
            throwNotFoundException(model);
        }

        DescribeLogGroupsResponse response = null;
        ListTagsLogGroupResponse tagsResponse = null;
        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                    ClientBuilder.getClient()::describeLogGroups);
            try {
                tagsResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToListTagsLogGroupRequest(model.getLogGroupName()),
                        ClientBuilder.getClient()::listTagsLogGroup);
            } catch (final CloudWatchLogsException e) {
                if (Translator.ACCESS_DENIED_ERROR_CODE.equals(e.awsErrorDetails().errorCode())) {
                    // fail silently, if there is no permission to list tags
                    logger.log(e.getMessage());
                } else {
                    throw e;
                }
            }
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        }

        final ResourceModel modelFromReadResult = Translator.translateForRead(response, tagsResponse);
        if (modelFromReadResult.getLogGroupName() == null) {
            throwNotFoundException(model);
        }

        return ProgressEvent.defaultSuccessHandler(modelFromReadResult);
    }

    private void throwNotFoundException(final ResourceModel model) {
        final ResourceModel nullSafeModel = model == null ? ResourceModel.builder().build() : model;
        throw new software.amazon.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
            Objects.toString(nullSafeModel.getPrimaryIdentifier()));
    }
}
