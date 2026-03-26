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
public class PurchaseRouter extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50604051611ff2380380611ff2833981810160405281019061003291906102e4565b61004e6100436101b560201b60201c565b6101bd60201b60201c565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16036100bd576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100b490610381565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff160361012c576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610123906103ed565b60405180910390fd5b81600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505061040d565b600033905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006102b182610286565b9050919050565b6102c1816102a6565b81146102cc57600080fd5b50565b6000815190506102de816102b8565b92915050565b600080604083850312156102fb576102fa610281565b5b6000610309858286016102cf565b925050602061031a858286016102cf565b9150509250929050565b600082825260208201905092915050565b7f496e76616c696420535346000000000000000000000000000000000000000000600082015250565b600061036b600b83610324565b915061037682610335565b602082019050919050565b6000602082019050818103600083015261039a8161035e565b9050919050565b7f496e76616c696420706c6174666f726d2077616c6c6574000000000000000000600082015250565b60006103d7601783610324565b91506103e2826103a1565b602082019050919050565b60006020820190508181036000830152610406816103ca565b9050919050565b611bd68061041c6000396000f3fe608060405234801561001057600080fd5b506004361061009e5760003560e01c8063c77836f911610066578063c77836f914610121578063f2fde38b1461013f578063f738c4a71461015b578063fa2af9da14610179578063fb74dabc146101975761009e565b80632f5eb679146100a3578063715018a6146100c15780638da5cb5b146100cb5780639c6868fc146100e9578063b3066d4914610105575b600080fd5b6100ab6101b5565b6040516100b89190610f74565b60405180910390f35b6100c96101db565b005b6100d36101ef565b6040516100e09190610f74565b60405180910390f35b61010360048036038101906100fe9190611005565b610218565b005b61011f600480360381019061011a9190611058565b610ab1565b005b610129610cce565b6040516101369190610f74565b60405180910390f35b610159600480360381019061015491906110ab565b610cf4565b005b610163610d77565b6040516101709190610f74565b60405180910390f35b610181610d9d565b60405161018e9190610f74565b60405180910390f35b61019f610dc3565b6040516101ac9190611137565b60405180910390f35b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6101e3610de9565b6101ed6000610e67565b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b610220610de9565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff160361028f576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610286906111af565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1603610320576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103179061121b565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16036103b1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103a890611287565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1603610442576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610439906112f3565b60405180910390fd5b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690506000600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690506000600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905060008373ffffffffffffffffffffffffffffffffffffffff1663e7572230876040518263ffffffff1660e01b81526004016104f29190611322565b602060405180830381865afa15801561050f573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906105339190611352565b905060008111610578576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161056f906113cb565b60405180910390fd5b60008473ffffffffffffffffffffffffffffffffffffffff166350b44712886040518263ffffffff1660e01b81526004016105b39190611322565b600060405180830381865afa1580156105d0573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f820116820180604052508101906105f99190611585565b5050505050505050905060008373ffffffffffffffffffffffffffffffffffffffff16631d645b12836040518263ffffffff1660e01b815260040161063e9190611322565b600060405180830381865afa15801561065b573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f8201168201806040525081019061068491906116bf565b505050509250505060008673ffffffffffffffffffffffffffffffffffffffff1663fac38f8e848a8d6040518463ffffffff1660e01b81526004016106cb9392919061177d565b602060405180830381865afa1580156106e8573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061070c9190611352565b9050818110610750576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161074790611800565b60405180910390fd5b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd8c600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16886040518463ffffffff1660e01b81526004016107d393929190611820565b6020604051808303816000875af11580156107f2573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906108169190611857565b905080610858576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161084f906118d0565b60405180910390fd5b8673ffffffffffffffffffffffffffffffffffffffff1663627c82f78a876040518363ffffffff1660e01b81526004016108939291906118f0565b600060405180830381600087803b1580156108ad57600080fd5b505af11580156108c1573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff16638cd9e9c28b6040518263ffffffff1660e01b81526004016108fe9190611322565b600060405180830381600087803b15801561091857600080fd5b505af115801561092c573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff166323b872dd600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168d8d6040518463ffffffff1660e01b815260040161098f93929190611820565b600060405180830381600087803b1580156109a957600080fd5b505af11580156109bd573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff16639b7377fa858b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168f6040518563ffffffff1660e01b8152600401610a229493929190611919565b600060405180830381600087803b158015610a3c57600080fd5b505af1158015610a50573d6000803e3d6000fd5b50505050888b73ffffffffffffffffffffffffffffffffffffffff168b7fef266bb11bf4b58aa8562ab8c8746e3b84a521780a2c57ca09d87bae13f5eb0988604051610a9c9190611322565b60405180910390a45050505050505050505050565b610ab9610de9565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610b28576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610b1f906119aa565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610b97576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610b8e90611a16565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610c06576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610bfd90611a82565b60405180910390fd5b82600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555081600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610cfc610de9565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610d6b576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610d6290611b14565b60405180910390fd5b610d7481610e67565b50565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610df1610f2b565b73ffffffffffffffffffffffffffffffffffffffff16610e0f6101ef565b73ffffffffffffffffffffffffffffffffffffffff1614610e65576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610e5c90611b80565b60405180910390fd5b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600033905090565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610f5e82610f33565b9050919050565b610f6e81610f53565b82525050565b6000602082019050610f896000830184610f65565b92915050565b6000604051905090565b600080fd5b600080fd5b610fac81610f53565b8114610fb757600080fd5b50565b600081359050610fc981610fa3565b92915050565b6000819050919050565b610fe281610fcf565b8114610fed57600080fd5b50565b600081359050610fff81610fd9565b92915050565b60008060006060848603121561101e5761101d610f99565b5b600061102c86828701610fba565b935050602061103d86828701610ff0565b925050604061104e86828701610ff0565b9150509250925092565b60008060006060848603121561107157611070610f99565b5b600061107f86828701610fba565b935050602061109086828701610fba565b92505060406110a186828701610fba565b9150509250925092565b6000602082840312156110c1576110c0610f99565b5b60006110cf84828501610fba565b91505092915050565b6000819050919050565b60006110fd6110f86110f384610f33565b6110d8565b610f33565b9050919050565b600061110f826110e2565b9050919050565b600061112182611104565b9050919050565b61113181611116565b82525050565b600060208201905061114c6000830184611128565b92915050565b600082825260208201905092915050565b7f496e76616c696420627579657200000000000000000000000000000000000000600082015250565b6000611199600d83611152565b91506111a482611163565b602082019050919050565b600060208201905081810360008301526111c88161118c565b9050919050565b7f5469636b65744e4654206e6f7420736574000000000000000000000000000000600082015250565b6000611205601183611152565b9150611210826111cf565b602082019050919050565b60006020820190508181036000830152611234816111f8565b9050919050565b7f536574746c656d656e74206e6f74207365740000000000000000000000000000600082015250565b6000611271601283611152565b915061127c8261123b565b602082019050919050565b600060208201905081810360008301526112a081611264565b9050919050565b7f4576656e744e4654206e6f742073657400000000000000000000000000000000600082015250565b60006112dd601083611152565b91506112e8826112a7565b602082019050919050565b6000602082019050818103600083015261130c816112d0565b9050919050565b61131c81610fcf565b82525050565b60006020820190506113376000830184611313565b92915050565b60008151905061134c81610fd9565b92915050565b60006020828403121561136857611367610f99565b5b60006113768482850161133d565b91505092915050565b7f496e76616c6964207469636b6574207072696365000000000000000000000000600082015250565b60006113b5601483611152565b91506113c08261137f565b602082019050919050565b600060208201905081810360008301526113e4816113a8565b9050919050565b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61143e826113f5565b810181811067ffffffffffffffff8211171561145d5761145c611406565b5b80604052505050565b6000611470610f8f565b905061147c8282611435565b919050565b600067ffffffffffffffff82111561149c5761149b611406565b5b6114a5826113f5565b9050602081019050919050565b60005b838110156114d05780820151818401526020810190506114b5565b60008484015250505050565b60006114ef6114ea84611481565b611466565b90508281526020810184848401111561150b5761150a6113f0565b5b6115168482856114b2565b509392505050565b600082601f830112611533576115326113eb565b5b81516115438482602086016114dc565b91505092915050565b600060ff82169050919050565b6115628161154c565b811461156d57600080fd5b50565b60008151905061157f81611559565b92915050565b60008060008060008060008060006101208a8c0312156115a8576115a7610f99565b5b60006115b68c828d0161133d565b99505060206115c78c828d0161133d565b98505060408a015167ffffffffffffffff8111156115e8576115e7610f9e565b5b6115f48c828d0161151e565b97505060606116058c828d0161133d565b96505060806116168c828d0161133d565b95505060a08a015167ffffffffffffffff81111561163757611636610f9e565b5b6116438c828d0161151e565b94505060c06116548c828d0161133d565b93505060e06116658c828d01611570565b9250506101006116778c828d0161133d565b9150509295985092959850929598565b60008115159050919050565b61169c81611687565b81146116a757600080fd5b50565b6000815190506116b981611693565b92915050565b600080600080600080600060e0888a0312156116de576116dd610f99565b5b600088015167ffffffffffffffff8111156116fc576116fb610f9e565b5b6117088a828b0161151e565b97505060206117198a828b0161133d565b965050604061172a8a828b0161133d565b955050606061173b8a828b0161133d565b945050608061174c8a828b0161133d565b93505060a061175d8a828b0161133d565b92505060c061176e8a828b016116aa565b91505092959891949750929550565b60006060820190506117926000830186611313565b61179f6020830185611313565b6117ac6040830184610f65565b949350505050565b7f45786365656473206d6178207065722077616c6c657400000000000000000000600082015250565b60006117ea601683611152565b91506117f5826117b4565b602082019050919050565b60006020820190508181036000830152611819816117dd565b9050919050565b60006060820190506118356000830186610f65565b6118426020830185610f65565b61184f6040830184611313565b949350505050565b60006020828403121561186d5761186c610f99565b5b600061187b848285016116aa565b91505092915050565b7f535346207472616e73666572206661696c656400000000000000000000000000600082015250565b60006118ba601383611152565b91506118c582611884565b602082019050919050565b600060208201905081810360008301526118e9816118ad565b9050919050565b60006040820190506119056000830185611313565b6119126020830184611313565b9392505050565b600060808201905061192e6000830187611313565b61193b6020830186611313565b6119486040830185610f65565b6119556060830184610f65565b95945050505050565b7f496e76616c6964205469636b65744e4654000000000000000000000000000000600082015250565b6000611994601183611152565b915061199f8261195e565b602082019050919050565b600060208201905081810360008301526119c381611987565b9050919050565b7f496e76616c696420536574746c656d656e740000000000000000000000000000600082015250565b6000611a00601283611152565b9150611a0b826119ca565b602082019050919050565b60006020820190508181036000830152611a2f816119f3565b9050919050565b7f496e76616c6964204576656e744e465400000000000000000000000000000000600082015250565b6000611a6c601083611152565b9150611a7782611a36565b602082019050919050565b60006020820190508181036000830152611a9b81611a5f565b9050919050565b7f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008201527f6464726573730000000000000000000000000000000000000000000000000000602082015250565b6000611afe602683611152565b9150611b0982611aa2565b604082019050919050565b60006020820190508181036000830152611b2d81611af1565b9050919050565b7f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572600082015250565b6000611b6a602083611152565b9150611b7582611b34565b602082019050919050565b60006020820190508181036000830152611b9981611b5d565b905091905056fea264697066735822122071def2ef19afcfe2b50aaa43637f42f8a47740cdd1f306932a7d5d592fa5599064736f6c634300081c0033";

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
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    public static final Event TICKETPURCHASED_EVENT = new Event("TicketPurchased",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }));;

    @Deprecated
    protected PurchaseRouter(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PurchaseRouter(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PurchaseRouter(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PurchaseRouter(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
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

    public static List<TicketPurchasedEventResponse> getTicketPurchasedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TICKETPURCHASED_EVENT,
            transactionReceipt);
        ArrayList<TicketPurchasedEventResponse> responses = new ArrayList<TicketPurchasedEventResponse>(
            valueList.size());
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

    public Flowable<TicketPurchasedEventResponse> ticketPurchasedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TICKETPURCHASED_EVENT));
        return ticketPurchasedEventFlowable(filter);
    }

    public RemoteFunctionCall<String> eventNFTAddress() {
        final Function function = new Function(FUNC_EVENTNFTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> platformWallet() {
        final Function function = new Function(FUNC_PLATFORMWALLET, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> purchaseTicket(String buyer, BigInteger ticketId,
        BigInteger sessionId) {
        final Function function = new Function(FUNC_PURCHASETICKET,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, buyer),
                new org.web3j.abi.datatypes.generated.Uint256(ticketId),
                new org.web3j.abi.datatypes.generated.Uint256(sessionId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setContracts(String _ticketNFT, String _settlement,
        String _eventNFT) {
        final Function function = new Function(FUNC_SETCONTRACTS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ticketNFT),
                new org.web3j.abi.datatypes.Address(160, _settlement),
                new org.web3j.abi.datatypes.Address(160, _eventNFT)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> settlementAddress() {
        final Function function = new Function(FUNC_SETTLEMENTADDRESS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ssfToken() {
        final Function function = new Function(FUNC_SSFTOKEN, Arrays.<Type>asList(),
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
    public static PurchaseRouter load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new PurchaseRouter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PurchaseRouter load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new PurchaseRouter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PurchaseRouter load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new PurchaseRouter(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PurchaseRouter load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new PurchaseRouter(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, transactionManager, contractGasProvider,
            getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PurchaseRouter> deploy(Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit, String _ssfToken, String _platformWallet) {
        String encodedConstructor = FunctionEncoder
            .encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _ssfToken),
                new org.web3j.abi.datatypes.Address(160, _platformWallet)));
        return deployRemoteCall(PurchaseRouter.class, web3j, transactionManager, gasPrice, gasLimit,
            getDeploymentBinary(), encodedConstructor);
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
