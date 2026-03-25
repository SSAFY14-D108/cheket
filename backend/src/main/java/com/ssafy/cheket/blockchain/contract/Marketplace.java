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
public class Marketplace extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b5060405161138f38038061138f83398181016040528101906100329190610274565b61004e61004361014560201b60201c565b61014d60201b60201c565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16036100bd576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100b4906102fe565b60405180910390fd5b80600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505061031e565b600033905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600061024182610216565b9050919050565b61025181610236565b811461025c57600080fd5b50565b60008151905061026e81610248565b92915050565b60006020828403121561028a57610289610211565b5b60006102988482850161025f565b91505092915050565b600082825260208201905092915050565b7f496e76616c6964205469636b65744e4654000000000000000000000000000000600082015250565b60006102e86011836102a1565b91506102f3826102b2565b602082019050919050565b60006020820190508181036000830152610317816102db565b9050919050565b6110628061032d6000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c8063715018a6146100675780638da5cb5b14610071578063b393391b1461008f578063c77836f9146100ad578063f2fde38b146100cb578063f83d1791146100e7575b600080fd5b61006f610103565b005b610079610117565b604051610086919061081c565b60405180910390f35b610097610140565b6040516100a49190610896565b60405180910390f35b6100b5610166565b6040516100c2919061081c565b60405180910390f35b6100e560048036038101906100e091906108f1565b61018c565b005b61010160048036038101906100fc9190610954565b61020f565b005b61010b610691565b610115600061070f565b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610194610691565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610203576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101fa90610a2a565b60405180910390fd5b61020c8161070f565b50565b610217610691565b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508373ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16636352211e846040518263ffffffff1660e01b815260040161028e9190610a59565b602060405180830381865afa1580156102ab573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102cf9190610a89565b73ffffffffffffffffffffffffffffffffffffffff1614610325576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161031c90610b02565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610394576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161038b90610b6e565b60405180910390fd5b8373ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610402576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103f990610bda565b60405180910390fd5b60008173ffffffffffffffffffffffffffffffffffffffff1663fb87774c846040518263ffffffff1660e01b815260040161043d9190610a59565b602060405180830381865afa15801561045a573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061047e9190610c33565b60ff16146104c1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016104b890610cac565b60405180910390fd5b8073ffffffffffffffffffffffffffffffffffffffff166323b872dd8585856040518463ffffffff1660e01b81526004016104fe93929190610ccc565b600060405180830381600087803b15801561051857600080fd5b505af115801561052c573d6000803e3d6000fd5b505050506000808273ffffffffffffffffffffffffffffffffffffffff166350b44712856040518263ffffffff1660e01b815260040161056c9190610a59565b600060405180830381865afa158015610589573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f820116820180604052508101906105b29190610e79565b50505050505050915091508273ffffffffffffffffffffffffffffffffffffffff16639b7377fa838389896040518563ffffffff1660e01b81526004016105fc9493929190610f7b565b600060405180830381600087803b15801561061657600080fd5b505af115801561062a573d6000803e3d6000fd5b505050508473ffffffffffffffffffffffffffffffffffffffff168673ffffffffffffffffffffffffffffffffffffffff16857f36e6fb4a154c9e03594558e15cd127b42b861ef40280d618b1e9c2ee5fde206a60405160405180910390a4505050505050565b6106996107d3565b73ffffffffffffffffffffffffffffffffffffffff166106b7610117565b73ffffffffffffffffffffffffffffffffffffffff161461070d576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016107049061100c565b60405180910390fd5b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600033905090565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610806826107db565b9050919050565b610816816107fb565b82525050565b6000602082019050610831600083018461080d565b92915050565b6000819050919050565b600061085c610857610852846107db565b610837565b6107db565b9050919050565b600061086e82610841565b9050919050565b600061088082610863565b9050919050565b61089081610875565b82525050565b60006020820190506108ab6000830184610887565b92915050565b6000604051905090565b600080fd5b600080fd5b6108ce816107fb565b81146108d957600080fd5b50565b6000813590506108eb816108c5565b92915050565b600060208284031215610907576109066108bb565b5b6000610915848285016108dc565b91505092915050565b6000819050919050565b6109318161091e565b811461093c57600080fd5b50565b60008135905061094e81610928565b92915050565b60008060006060848603121561096d5761096c6108bb565b5b600061097b868287016108dc565b935050602061098c868287016108dc565b925050604061099d8682870161093f565b9150509250925092565b600082825260208201905092915050565b7f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008201527f6464726573730000000000000000000000000000000000000000000000000000602082015250565b6000610a146026836109a7565b9150610a1f826109b8565b604082019050919050565b60006020820190508181036000830152610a4381610a07565b9050919050565b610a538161091e565b82525050565b6000602082019050610a6e6000830184610a4a565b92915050565b600081519050610a83816108c5565b92915050565b600060208284031215610a9f57610a9e6108bb565b5b6000610aad84828501610a74565b91505092915050565b7f4e6f74207469636b6574206f776e657200000000000000000000000000000000600082015250565b6000610aec6010836109a7565b9150610af782610ab6565b602082019050919050565b60006020820190508181036000830152610b1b81610adf565b9050919050565b7f496e76616c696420616464726573730000000000000000000000000000000000600082015250565b6000610b58600f836109a7565b9150610b6382610b22565b602082019050919050565b60006020820190508181036000830152610b8781610b4b565b9050919050565b7f43616e6e6f74207472616e7366657220746f2073656c66000000000000000000600082015250565b6000610bc46017836109a7565b9150610bcf82610b8e565b602082019050919050565b60006020820190508181036000830152610bf381610bb7565b9050919050565b600060ff82169050919050565b610c1081610bfa565b8114610c1b57600080fd5b50565b600081519050610c2d81610c07565b92915050565b600060208284031215610c4957610c486108bb565b5b6000610c5784828501610c1e565b91505092915050565b7f5469636b6574206e6f742076616c696400000000000000000000000000000000600082015250565b6000610c966010836109a7565b9150610ca182610c60565b602082019050919050565b60006020820190508181036000830152610cc581610c89565b9050919050565b6000606082019050610ce1600083018661080d565b610cee602083018561080d565b610cfb6040830184610a4a565b949350505050565b600081519050610d1281610928565b92915050565b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b610d6b82610d22565b810181811067ffffffffffffffff82111715610d8a57610d89610d33565b5b80604052505050565b6000610d9d6108b1565b9050610da98282610d62565b919050565b600067ffffffffffffffff821115610dc957610dc8610d33565b5b610dd282610d22565b9050602081019050919050565b60005b83811015610dfd578082015181840152602081019050610de2565b60008484015250505050565b6000610e1c610e1784610dae565b610d93565b905082815260208101848484011115610e3857610e37610d1d565b5b610e43848285610ddf565b509392505050565b600082601f830112610e6057610e5f610d18565b5b8151610e70848260208601610e09565b91505092915050565b60008060008060008060008060006101208a8c031215610e9c57610e9b6108bb565b5b6000610eaa8c828d01610d03565b9950506020610ebb8c828d01610d03565b98505060408a015167ffffffffffffffff811115610edc57610edb6108c0565b5b610ee88c828d01610e4b565b9750506060610ef98c828d01610d03565b9650506080610f0a8c828d01610d03565b95505060a08a015167ffffffffffffffff811115610f2b57610f2a6108c0565b5b610f378c828d01610e4b565b94505060c0610f488c828d01610d03565b93505060e0610f598c828d01610c1e565b925050610100610f6b8c828d01610d03565b9150509295985092959850929598565b6000608082019050610f906000830187610a4a565b610f9d6020830186610a4a565b610faa604083018561080d565b610fb7606083018461080d565b95945050505050565b7f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572600082015250565b6000610ff66020836109a7565b915061100182610fc0565b602082019050919050565b6000602082019050818103600083015261102581610fe9565b905091905056fea2646970667358221220ef5a8f667965f7124e871b6174f7d8dc7e670243e848fc2da38375d07ee8988f64736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_DIRECTTRANSFER = "directTransfer";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_TICKETNFT = "ticketNFT";

    public static final String FUNC_TICKETNFTADDRESS = "ticketNFTAddress";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event DIRECTTRANSFERRED_EVENT = new Event("DirectTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    @Deprecated
    protected Marketplace(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Marketplace(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Marketplace(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Marketplace(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DirectTransferredEventResponse> getDirectTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DIRECTTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<DirectTransferredEventResponse> responses = new ArrayList<DirectTransferredEventResponse>(valueList.size());
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

    public Flowable<DirectTransferredEventResponse> directTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDirectTransferredEventFromLog(log));
    }

    public Flowable<DirectTransferredEventResponse> directTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DIRECTTRANSFERRED_EVENT));
        return directTransferredEventFlowable(filter);
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

    public RemoteFunctionCall<TransactionReceipt> directTransfer(String from, String to,
            BigInteger ticketId) {
        final Function function = new Function(
                FUNC_DIRECTTRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(ticketId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> ticketNFT() {
        final Function function = new Function(FUNC_TICKETNFT, 
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
    public static Marketplace load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new Marketplace(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Marketplace load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Marketplace(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Marketplace load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new Marketplace(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Marketplace load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Marketplace(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Marketplace> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<Marketplace> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Marketplace> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Marketplace> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Marketplace.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
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
