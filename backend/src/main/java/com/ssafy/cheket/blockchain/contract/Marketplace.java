package com.ssafy.cheket.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>
 * Auto generated code.
 * <p>
 * <strong>Do not modify!</strong>
 * <p>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j
 * command line tools</a>, or the
 * org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a href=
 * "https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen
 * module</a> to update.
 *
 * <p>
 * Generated with web3j version 4.12.3.
 */
@SuppressWarnings("rawtypes")
public class Marketplace extends Contract {
    public static final String BINARY = "0x60803461010657601f61085c38819003918201601f19168301916001600160401b0383118484101761010b5780849260209460405283398101031261010657516001600160a01b038116908190036101065760008054336001600160a01b03198216811783556040519290916001600160a01b0316907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a381156100d05750600180546001600160a01b0319908116831790915560028054909116909117905560405161073a90816101228239f35b62461bcd60e51b8152602060048201526011602482015270125b9d985b1a5908151a58dad95d139195607a1b6044820152606490fd5b600080fd5b634e487b7160e01b600052604160045260246000fdfe6080604052600436101561001257600080fd5b6000803560e01c8063715018a6146105815780638da5cb5b1461055a578063b393391b14610531578063c77836f914610508578063f2fde38b146104425763f83d17911461005f57600080fd5b3461043f57606036600319011261043f576100786105db565b6024356001600160a01b038116919082900361043d576044356100996106ac565b6002546040516331a9108f60e11b815260048101839052926001600160a01b0390911690602084602481855afa9384156102c05786946103f9575b506001600160a01b0390811693168390036103c157831561038a5782841461034557604051633ee1ddd360e21b815260048101839052602081602481855afa80156102c0578690610307575b60ff9150166102cf57803b156102cb576040516323b872dd60e01b8152836004820152846024820152826044820152858160648183865af180156102c0576102ac575b50849060405163285a238960e11b81528360048201528281602481855afa80156102a1578391849161021e575b50823b1561020f5760848492836040519586948593634db9bbfd60e11b8552600485015260248401528960448401528a60648401525af18015610213576101fa575b50807f36e6fb4a154c9e03594558e15cd127b42b861ef40280d618b1e9c2ee5fde206a91a480f35b81610204916105f6565b61020f5783386101d2565b8380fd5b6040513d84823e3d90fd5b9150503d8084833e61023081836105f6565b8101906101208183031261020f57805190602081015192604082015167ffffffffffffffff811161029d578161026791840161063c565b5060a08201519167ffffffffffffffff831161029d5761028e60e09261029594830161063c565b500161062e565b509038610190565b8680fd5b6040513d85823e3d90fd5b856102b9919692966105f6565b9338610163565b6040513d88823e3d90fd5b8480fd5b60405162461bcd60e51b815260206004820152601060248201526f151a58dad95d081b9bdd081d985b1a5960821b6044820152606490fd5b506020813d60201161033d575b81610321602093836105f6565b810103126103395761033460ff9161062e565b610120565b8580fd5b3d9150610314565b60405162461bcd60e51b815260206004820152601760248201527f43616e6e6f74207472616e7366657220746f2073656c660000000000000000006044820152606490fd5b60405162461bcd60e51b815260206004820152600f60248201526e496e76616c6964206164647265737360881b6044820152606490fd5b60405162461bcd60e51b815260206004820152601060248201526f2737ba103a34b1b5b2ba1037bbb732b960811b6044820152606490fd5b9093506020813d602011610435575b81610415602093836105f6565b8101031261033957516001600160a01b03811681036103395792386100d4565b3d9150610408565b825b80fd5b503461043f57602036600319011261043f5761045c6105db565b6104646106ac565b6001600160a01b031680156104b45781546001600160a01b03198116821783556001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08380a380f35b60405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608490fd5b503461043f578060031936011261043f576002546040516001600160a01b039091168152602090f35b503461043f578060031936011261043f576001546040516001600160a01b039091168152602090f35b503461043f578060031936011261043f57546040516001600160a01b039091168152602090f35b503461043f578060031936011261043f5761059a6106ac565b80546001600160a01b03198116825581906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a380f35b600435906001600160a01b03821682036105f157565b600080fd5b90601f8019910116810190811067ffffffffffffffff82111761061857604052565b634e487b7160e01b600052604160045260246000fd5b519060ff821682036105f157565b81601f820112156105f15780519067ffffffffffffffff82116106185760405192610671601f8401601f1916602001856105f6565b828452602083830101116105f15760005b82811061069757505060206000918301015290565b80602080928401015182828701015201610682565b6000546001600160a01b031633036106c057565b606460405162461bcd60e51b815260206004820152602060248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152fdfea2646970667358221220d90710821fb0874e875e2c2803b464cacc5eed6f423a0a5843b4776efc8be06664736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_DIRECTTRANSFER = "directTransfer";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_TICKETNFT = "ticketNFT";

    public static final String FUNC_TICKETNFTADDRESS = "ticketNFTAddress";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event DIRECTTRANSFERRED_EVENT = new Event("DirectTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    @Deprecated
    protected Marketplace(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Marketplace(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Marketplace(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Marketplace(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DirectTransferredEventResponse> getDirectTransferredEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DIRECTTRANSFERRED_EVENT,
            transactionReceipt);
        ArrayList<DirectTransferredEventResponse> responses = new ArrayList<DirectTransferredEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DirectTransferredEventResponse typedResponse = new DirectTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.from = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DirectTransferredEventResponse getDirectTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DIRECTTRANSFERRED_EVENT, log);
        DirectTransferredEventResponse typedResponse = new DirectTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.from = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<DirectTransferredEventResponse> directTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDirectTransferredEventFromLog(log));
    }

    public Flowable<DirectTransferredEventResponse> directTransferredEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DIRECTTRANSFERRED_EVENT));
        return directTransferredEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT,
            transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> directTransfer(String from, String to, BigInteger ticketId) {
        final Function function = new Function(FUNC_DIRECTTRANSFER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to), new org.web3j.abi.datatypes.generated.Uint256(ticketId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> ticketNFT() {
        final Function function = new Function(FUNC_TICKETNFT, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ticketNFTAddress() {
        final Function function = new Function(FUNC_TICKETNFTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Marketplace load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new Marketplace(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Marketplace load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new Marketplace(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Marketplace load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new Marketplace(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Marketplace load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new Marketplace(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Marketplace> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    public static RemoteCall<Marketplace> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, transactionManager, contractGasProvider,
            getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Marketplace> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Marketplace> deploy(Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class DirectTransferredEventResponse extends BaseEventResponse {
        public BigInteger ticketId;

        public String from;

        public String to;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }
}
