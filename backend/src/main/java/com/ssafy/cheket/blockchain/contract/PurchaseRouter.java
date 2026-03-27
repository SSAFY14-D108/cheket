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
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.12.3.
 */
@SuppressWarnings("rawtypes")
public class PurchaseRouter extends Contract {
    public static final String BINARY = "0x60803461016057601f610e1138819003918201601f19168301916001600160401b0383118484101761016557808492604094855283398101031261016057610052602061004b8361017b565b920161017b565b60008054336001600160a01b0319821681178355604051949290916001600160a01b0316907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a36001600160a01b031691821561013057506001600160a01b03169081156100eb5760018060a01b0319600154161760015560018060a01b03196005541617600555604051610c8190816101908239f35b60405162461bcd60e51b815260206004820152601760248201527f496e76616c696420706c6174666f726d2077616c6c65740000000000000000006044820152606490fd5b62461bcd60e51b815260206004820152600b60248201526a24b73b30b634b21029a9a360a91b6044820152606490fd5b600080fd5b634e487b7160e01b600052604160045260246000fd5b51906001600160a01b03821682036101605756fe608080604052600436101561001357600080fd5b600090813560e01c9081632f5eb67914610afe57508063715018a614610aa45780638da5cb5b14610a7d5780639c6868fc146103ad578063b3066d49146101fb578063c77836f9146101d2578063f2fde38b1461010c578063f738c4a7146100e3578063fa2af9da146100ba5763fb74dabc1461008f57600080fd5b346100b757806003193601126100b7576001546040516001600160a01b039091168152602090f35b80fd5b50346100b757806003193601126100b7576005546040516001600160a01b039091168152602090f35b50346100b757806003193601126100b7576003546040516001600160a01b039091168152602090f35b50346100b75760203660031901126100b757610126610b23565b61012e610bf3565b6001600160a01b0316801561017e5781546001600160a01b03198116821783556001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08380a380f35b60405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608490fd5b50346100b757806003193601126100b7576002546040516001600160a01b039091168152602090f35b50346100b75760603660031901126100b757610215610b23565b6024356001600160a01b038116908190036103a9576044356001600160a01b03811691908290036103a557610248610bf3565b6005549260ff8460a01c1661036a576001600160a01b031680156103315781156102f75782156102bf576001600160601b0360a01b60025416176002556001600160601b0360a01b60035416176003556001600160601b0360a01b6004541617600455600160a01b9060ff60a01b19161760055580f35b60405162461bcd60e51b815260206004820152601060248201526f125b9d985b1a5908115d995b9d13919560821b6044820152606490fd5b60405162461bcd60e51b8152602060048201526012602482015271125b9d985b1a590814d95d1d1b195b595b9d60721b6044820152606490fd5b60405162461bcd60e51b8152602060048201526011602482015270125b9d985b1a5908151a58dad95d139195607a1b6044820152606490fd5b60405162461bcd60e51b8152602060048201526013602482015272105b1c9958591e481a5b9a5d1a585b1a5e9959606a1b6044820152606490fd5b8380fd5b8280fd5b50346100b75760603660031901126100b7576103c7610b23565b604435906024356103d6610bf3565b6001600160a01b038216918215610a48576002546001600160a01b03168015610a0f576003546001600160a01b03169182156109d5576004546001600160a01b031690811561099d57604051630e75722360e41b81526004810186905293602085602481875afa948515610703578995610965575b5084156109295760405163285a238960e11b815260048101879052928984602481885afa801561078b5789908b958c916108a4575b500361086c578960249160405192838092630eb22d8960e11b82528860048301525afa90811561078b578a91610815575b50604051637d61c7c760e11b8152846004820152896024820152886044820152602081606481895afa90811561080a578b916107d4575b501015610796576001546040516323b872dd60e01b81526001600160a01b0384811660048301528381166024830152604482018890529091602091839160649183918f91165af190811561078b578a9161074d575b5015610712578089913b1561070e57819060446040518094819363627c82f760e01b83528d60048401528a60248401525af18015610703576106eb575b5090829188933b156103a55760405163466cf4e160e11b815260048101879052848160248183885af19081156106e05785916106cb575b50506005546001600160a01b0316833b1561069c576040516323b872dd60e01b81526001600160a01b0391821660048201529116602482015260448101869052838160648183875af19081156106c05784916106ab575b50506005546001600160a01b0316823b156103a55760848492836040519586948593634db9bbfd60e11b855260048501528c602485015260448401528a60648401525af180156106a057610687575b505060207fef266bb11bf4b58aa8562ab8c8746e3b84a521780a2c57ca09d87bae13f5eb0991604051908152a480f35b8161069191610b3e565b61069c578438610657565b8480fd5b6040513d84823e3d90fd5b816106b591610b3e565b6103a9578238610608565b6040513d86823e3d90fd5b816106d591610b3e565b6103a55783386105b1565b6040513d87823e3d90fd5b886106fa919992949399610b3e565b9690913861057a565b6040513d8b823e3d90fd5b5080fd5b60405162461bcd60e51b815260206004820152601360248201527214d4d1881d1c985b9cd9995c8819985a5b1959606a1b6044820152606490fd5b90506020813d602011610783575b8161076860209383610b3e565b8101031261077f5761077990610be6565b3861053d565b8980fd5b3d915061075b565b6040513d8c823e3d90fd5b60405162461bcd60e51b8152602060048201526016602482015275115e18d959591cc81b585e081c195c881dd85b1b195d60521b6044820152606490fd5b90506020813d602011610802575b816107ef60209383610b3e565b810103126107fe5751386104e8565b8a80fd5b3d91506107e2565b6040513d8d823e3d90fd5b90503d808b833e6108268183610b3e565b810160e0828203126107fe5781519067ffffffffffffffff82116108685761084f918301610b76565b5061086160c060408301519201610be6565b50386104b1565b8b80fd5b60405162461bcd60e51b815260206004820152601060248201526f0a6cae6e6d2dedc40dad2e6dac2e8c6d60831b6044820152606490fd5b955050503d808b863e6108b78186610b3e565b8401610120858203126107fe57845190602086015195604081015167ffffffffffffffff811161092557826108ed918301610b76565b5060a081015167ffffffffffffffff81116109255760e092610910918301610b76565b50015160ff8116036107fe5789909438610480565b8d80fd5b60405162461bcd60e51b8152602060048201526014602482015273496e76616c6964207469636b657420707269636560601b6044820152606490fd5b9094506020813d602011610995575b8161098160209383610b3e565b810103126109915751933861044b565b8880fd5b3d9150610974565b60405162461bcd60e51b815260206004820152601060248201526f115d995b9d139195081b9bdd081cd95d60821b6044820152606490fd5b60405162461bcd60e51b815260206004820152601260248201527114d95d1d1b195b595b9d081b9bdd081cd95d60721b6044820152606490fd5b60405162461bcd60e51b8152602060048201526011602482015270151a58dad95d139195081b9bdd081cd95d607a1b6044820152606490fd5b60405162461bcd60e51b815260206004820152600d60248201526c24b73b30b634b210313abcb2b960991b6044820152606490fd5b50346100b757806003193601126100b757546040516001600160a01b039091168152602090f35b50346100b757806003193601126100b757610abd610bf3565b80546001600160a01b03198116825581906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a380f35b90503461070e578160031936011261070e576004546001600160a01b03168152602090f35b600435906001600160a01b0382168203610b3957565b600080fd5b90601f8019910116810190811067ffffffffffffffff821117610b6057604052565b634e487b7160e01b600052604160045260246000fd5b81601f82011215610b395780519067ffffffffffffffff8211610b605760405192610bab601f8401601f191660200185610b3e565b82845260208383010111610b395760005b828110610bd157505060206000918301015290565b80602080928401015182828701015201610bbc565b51908115158203610b3957565b6000546001600160a01b03163303610c0757565b606460405162461bcd60e51b815260206004820152602060248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152fdfea2646970667358221220c2dec317c3f255b4d75155e9cd432d17baea874c3274f5f710b1d8d8a5b843ce64736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_EVENTNFTADDRESS = "eventNFTAddress";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PLATFORMWALLET = "platformWallet";

    public static final String FUNC_PURCHASETICKET = "purchaseTicket";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SETCONTRACTS = "setContracts";

    public static final String FUNC_SETTLEMENTADDRESS = "settlementAddress";

    public static final String FUNC_SSFTOKEN = "ssfToken";

    public static final String FUNC_TICKETNFTADDRESS = "ticketNFTAddress";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event TICKETPURCHASED_EVENT = new Event("TicketPurchased", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected PurchaseRouter(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PurchaseRouter(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PurchaseRouter(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PurchaseRouter(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
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

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<TicketPurchasedEventResponse> getTicketPurchasedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TICKETPURCHASED_EVENT, transactionReceipt);
        ArrayList<TicketPurchasedEventResponse> responses = new ArrayList<TicketPurchasedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TicketPurchasedEventResponse typedResponse = new TicketPurchasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TicketPurchasedEventResponse getTicketPurchasedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TICKETPURCHASED_EVENT, log);
        TicketPurchasedEventResponse typedResponse = new TicketPurchasedEventResponse();
        typedResponse.log = log;
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.sessionId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TicketPurchasedEventResponse> ticketPurchasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTicketPurchasedEventFromLog(log));
    }

    public Flowable<TicketPurchasedEventResponse> ticketPurchasedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TICKETPURCHASED_EVENT));
        return ticketPurchasedEventFlowable(filter);
    }

    public RemoteFunctionCall<String> eventNFTAddress() {
        final Function function = new Function(FUNC_EVENTNFTADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> platformWallet() {
        final Function function = new Function(FUNC_PLATFORMWALLET, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> purchaseTicket(String buyer, BigInteger ticketId,
            BigInteger sessionId) {
        final Function function = new Function(
                FUNC_PURCHASETICKET, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, buyer), 
                new org.web3j.abi.datatypes.generated.Uint256(ticketId), 
                new org.web3j.abi.datatypes.generated.Uint256(sessionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setContracts(String _ticketNFT,
            String _settlement, String _eventNFT) {
        final Function function = new Function(
                FUNC_SETCONTRACTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT), 
                new org.web3j.abi.datatypes.Address(160, _settlement), 
                new org.web3j.abi.datatypes.Address(160, _eventNFT)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> settlementAddress() {
        final Function function = new Function(FUNC_SETTLEMENTADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ssfToken() {
        final Function function = new Function(FUNC_SSFTOKEN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ticketNFTAddress() {
        final Function function = new Function(FUNC_TICKETNFTADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static PurchaseRouter load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new PurchaseRouter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PurchaseRouter load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PurchaseRouter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PurchaseRouter load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new PurchaseRouter(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PurchaseRouter load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PurchaseRouter(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken), 
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider,
            String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken), 
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken), 
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit,
            String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken), 
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
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

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class TicketPurchasedEventResponse extends BaseEventResponse {
        public BigInteger ticketId;

        public String buyer;

        public BigInteger sessionId;

        public BigInteger price;
    }
}
