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
    public static final String BINARY = "0x608060405234801561001057600080fd5b506040516120ac3803806120ac833981810160405281019061003291906102e4565b61004e6100436101b560201b60201c565b6101bd60201b60201c565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16036100bd576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100b490610381565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff160361012c576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610123906103ed565b60405180910390fd5b81600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505061040d565b600033905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006102b182610286565b9050919050565b6102c1816102a6565b81146102cc57600080fd5b50565b6000815190506102de816102b8565b92915050565b600080604083850312156102fb576102fa610281565b5b6000610309858286016102cf565b925050602061031a858286016102cf565b9150509250929050565b600082825260208201905092915050565b7f496e76616c696420535346000000000000000000000000000000000000000000600082015250565b600061036b600b83610324565b915061037682610335565b602082019050919050565b6000602082019050818103600083015261039a8161035e565b9050919050565b7f496e76616c696420706c6174666f726d2077616c6c6574000000000000000000600082015250565b60006103d7601783610324565b91506103e2826103a1565b602082019050919050565b60006020820190508181036000830152610406816103ca565b9050919050565b611c908061041c6000396000f3fe608060405234801561001057600080fd5b506004361061009e5760003560e01c8063c77836f911610066578063c77836f914610121578063f2fde38b1461013f578063f738c4a71461015b578063fa2af9da14610179578063fb74dabc146101975761009e565b80632f5eb679146100a3578063715018a6146100c15780638da5cb5b146100cb5780639c6868fc146100e9578063b3066d4914610105575b600080fd5b6100ab6101b5565b6040516100b89190610fc2565b60405180910390f35b6100c96101db565b005b6100d36101ef565b6040516100e09190610fc2565b60405180910390f35b61010360048036038101906100fe9190611053565b610218565b005b61011f600480360381019061011a91906110a6565b610aff565b005b610129610d1c565b6040516101369190610fc2565b60405180910390f35b610159600480360381019061015491906110f9565b610d42565b005b610163610dc5565b6040516101709190610fc2565b60405180910390f35b610181610deb565b60405161018e9190610fc2565b60405180910390f35b61019f610e11565b6040516101ac9190611185565b60405180910390f35b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6101e3610e37565b6101ed6000610eb5565b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b610220610e37565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff160361028f576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610286906111fd565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1603610320576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161031790611269565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16036103b1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103a8906112d5565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff16600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1603610442576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161043990611341565b60405180910390fd5b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690506000600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690506000600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905060008373ffffffffffffffffffffffffffffffffffffffff1663e7572230876040518263ffffffff1660e01b81526004016104f29190611370565b602060405180830381865afa15801561050f573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061053391906113a0565b905060008111610578576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161056f90611419565b60405180910390fd5b60008473ffffffffffffffffffffffffffffffffffffffff166350b44712886040518263ffffffff1660e01b81526004016105b39190611370565b600060405180830381865afa1580156105d0573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f820116820180604052508101906105f991906115d3565b505050505050505090508273ffffffffffffffffffffffffffffffffffffffff1663591ed976826040518263ffffffff1660e01b815260040161063c9190611370565b602060405180830381865afa158015610659573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061067d919061170d565b6106bc576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016106b390611786565b60405180910390fd5b60008373ffffffffffffffffffffffffffffffffffffffff16631d645b12836040518263ffffffff1660e01b81526004016106f79190611370565b600060405180830381865afa158015610714573d6000803e3d6000fd5b505050506040513d6000823e3d601f19601f8201168201806040525081019061073d91906117a6565b505050509250505060008673ffffffffffffffffffffffffffffffffffffffff1663fac38f8e848a8d6040518463ffffffff1660e01b815260040161078493929190611864565b602060405180830381865afa1580156107a1573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906107c591906113a0565b9050818110610809576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610800906118e7565b60405180910390fd5b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd8c600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16886040518463ffffffff1660e01b815260040161088c93929190611907565b6020604051808303816000875af11580156108ab573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906108cf919061170d565b905080610911576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016109089061198a565b60405180910390fd5b8673ffffffffffffffffffffffffffffffffffffffff1663627c82f78a876040518363ffffffff1660e01b815260040161094c9291906119aa565b600060405180830381600087803b15801561096657600080fd5b505af115801561097a573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff166323b872dd600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168d8d6040518463ffffffff1660e01b81526004016109dd93929190611907565b600060405180830381600087803b1580156109f757600080fd5b505af1158015610a0b573d6000803e3d6000fd5b505050508773ffffffffffffffffffffffffffffffffffffffff16639b7377fa858b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168f6040518563ffffffff1660e01b8152600401610a7094939291906119d3565b600060405180830381600087803b158015610a8a57600080fd5b505af1158015610a9e573d6000803e3d6000fd5b50505050888b73ffffffffffffffffffffffffffffffffffffffff168b7fef266bb11bf4b58aa8562ab8c8746e3b84a521780a2c57ca09d87bae13f5eb0988604051610aea9190611370565b60405180910390a45050505050505050505050565b610b07610e37565b600073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1603610b76576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610b6d90611a64565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1603610be5576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610bdc90611ad0565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610c54576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610c4b90611b3c565b60405180910390fd5b82600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555081600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610d4a610e37565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1603610db9576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610db090611bce565b60405180910390fd5b610dc281610eb5565b50565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b610e3f610f79565b73ffffffffffffffffffffffffffffffffffffffff16610e5d6101ef565b73ffffffffffffffffffffffffffffffffffffffff1614610eb3576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610eaa90611c3a565b60405180910390fd5b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600033905090565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610fac82610f81565b9050919050565b610fbc81610fa1565b82525050565b6000602082019050610fd76000830184610fb3565b92915050565b6000604051905090565b600080fd5b600080fd5b610ffa81610fa1565b811461100557600080fd5b50565b60008135905061101781610ff1565b92915050565b6000819050919050565b6110308161101d565b811461103b57600080fd5b50565b60008135905061104d81611027565b92915050565b60008060006060848603121561106c5761106b610fe7565b5b600061107a86828701611008565b935050602061108b8682870161103e565b925050604061109c8682870161103e565b9150509250925092565b6000806000606084860312156110bf576110be610fe7565b5b60006110cd86828701611008565b93505060206110de86828701611008565b92505060406110ef86828701611008565b9150509250925092565b60006020828403121561110f5761110e610fe7565b5b600061111d84828501611008565b91505092915050565b6000819050919050565b600061114b61114661114184610f81565b611126565b610f81565b9050919050565b600061115d82611130565b9050919050565b600061116f82611152565b9050919050565b61117f81611164565b82525050565b600060208201905061119a6000830184611176565b92915050565b600082825260208201905092915050565b7f496e76616c696420627579657200000000000000000000000000000000000000600082015250565b60006111e7600d836111a0565b91506111f2826111b1565b602082019050919050565b60006020820190508181036000830152611216816111da565b9050919050565b7f5469636b65744e4654206e6f7420736574000000000000000000000000000000600082015250565b60006112536011836111a0565b915061125e8261121d565b602082019050919050565b6000602082019050818103600083015261128281611246565b9050919050565b7f536574746c656d656e74206e6f74207365740000000000000000000000000000600082015250565b60006112bf6012836111a0565b91506112ca82611289565b602082019050919050565b600060208201905081810360008301526112ee816112b2565b9050919050565b7f4576656e744e4654206e6f742073657400000000000000000000000000000000600082015250565b600061132b6010836111a0565b9150611336826112f5565b602082019050919050565b6000602082019050818103600083015261135a8161131e565b9050919050565b61136a8161101d565b82525050565b60006020820190506113856000830184611361565b92915050565b60008151905061139a81611027565b92915050565b6000602082840312156113b6576113b5610fe7565b5b60006113c48482850161138b565b91505092915050565b7f496e76616c6964207469636b6574207072696365000000000000000000000000600082015250565b60006114036014836111a0565b915061140e826113cd565b602082019050919050565b60006020820190508181036000830152611432816113f6565b9050919050565b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b61148c82611443565b810181811067ffffffffffffffff821117156114ab576114aa611454565b5b80604052505050565b60006114be610fdd565b90506114ca8282611483565b919050565b600067ffffffffffffffff8211156114ea576114e9611454565b5b6114f382611443565b9050602081019050919050565b60005b8381101561151e578082015181840152602081019050611503565b60008484015250505050565b600061153d611538846114cf565b6114b4565b9050828152602081018484840111156115595761155861143e565b5b611564848285611500565b509392505050565b600082601f83011261158157611580611439565b5b815161159184826020860161152a565b91505092915050565b600060ff82169050919050565b6115b08161159a565b81146115bb57600080fd5b50565b6000815190506115cd816115a7565b92915050565b60008060008060008060008060006101208a8c0312156115f6576115f5610fe7565b5b60006116048c828d0161138b565b99505060206116158c828d0161138b565b98505060408a015167ffffffffffffffff81111561163657611635610fec565b5b6116428c828d0161156c565b97505060606116538c828d0161138b565b96505060806116648c828d0161138b565b95505060a08a015167ffffffffffffffff81111561168557611684610fec565b5b6116918c828d0161156c565b94505060c06116a28c828d0161138b565b93505060e06116b38c828d016115be565b9250506101006116c58c828d0161138b565b9150509295985092959850929598565b60008115159050919050565b6116ea816116d5565b81146116f557600080fd5b50565b600081519050611707816116e1565b92915050565b60006020828403121561172357611722610fe7565b5b6000611731848285016116f8565b91505092915050565b7f426f6f6b696e67206e6f74206f70656e00000000000000000000000000000000600082015250565b60006117706010836111a0565b915061177b8261173a565b602082019050919050565b6000602082019050818103600083015261179f81611763565b9050919050565b600080600080600080600060e0888a0312156117c5576117c4610fe7565b5b600088015167ffffffffffffffff8111156117e3576117e2610fec565b5b6117ef8a828b0161156c565b97505060206118008a828b0161138b565b96505060406118118a828b0161138b565b95505060606118228a828b0161138b565b94505060806118338a828b0161138b565b93505060a06118448a828b0161138b565b92505060c06118558a828b016116f8565b91505092959891949750929550565b60006060820190506118796000830186611361565b6118866020830185611361565b6118936040830184610fb3565b949350505050565b7f45786365656473206d6178207065722077616c6c657400000000000000000000600082015250565b60006118d16016836111a0565b91506118dc8261189b565b602082019050919050565b60006020820190508181036000830152611900816118c4565b9050919050565b600060608201905061191c6000830186610fb3565b6119296020830185610fb3565b6119366040830184611361565b949350505050565b7f535346207472616e73666572206661696c656400000000000000000000000000600082015250565b60006119746013836111a0565b915061197f8261193e565b602082019050919050565b600060208201905081810360008301526119a381611967565b9050919050565b60006040820190506119bf6000830185611361565b6119cc6020830184611361565b9392505050565b60006080820190506119e86000830187611361565b6119f56020830186611361565b611a026040830185610fb3565b611a0f6060830184610fb3565b95945050505050565b7f496e76616c6964205469636b65744e4654000000000000000000000000000000600082015250565b6000611a4e6011836111a0565b9150611a5982611a18565b602082019050919050565b60006020820190508181036000830152611a7d81611a41565b9050919050565b7f496e76616c696420536574746c656d656e740000000000000000000000000000600082015250565b6000611aba6012836111a0565b9150611ac582611a84565b602082019050919050565b60006020820190508181036000830152611ae981611aad565b9050919050565b7f496e76616c6964204576656e744e465400000000000000000000000000000000600082015250565b6000611b266010836111a0565b9150611b3182611af0565b602082019050919050565b60006020820190508181036000830152611b5581611b19565b9050919050565b7f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008201527f6464726573730000000000000000000000000000000000000000000000000000602082015250565b6000611bb86026836111a0565b9150611bc382611b5c565b604082019050919050565b60006020820190508181036000830152611be781611bab565b9050919050565b7f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572600082015250565b6000611c246020836111a0565b9150611c2f82611bee565b602082019050919050565b60006020820190508181036000830152611c5381611c17565b905091905056fea2646970667358221220a90a5d4b205aa37907242c725ab492caf3946fc25e3c6ad194618537f9c0dd1a64736f6c634300081c0033";

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
