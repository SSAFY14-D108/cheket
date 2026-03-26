package com.ssafy.cheket.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple7;
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
public class Escrow extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b506040516123b03803806123b0833981810160405281019061003291906102e4565b61004e6100436101b560201b60201c565b6101bd60201b60201c565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff16036100bd576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100b490610381565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff160361012c576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610123906103ed565b60405180910390fd5b81600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550505061040d565b600033905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006102b182610286565b9050919050565b6102c1816102a6565b81146102cc57600080fd5b50565b6000815190506102de816102b8565b92915050565b600080604083850312156102fb576102fa610281565b5b6000610309858286016102cf565b925050602061031a858286016102cf565b9150509250929050565b600082825260208201905092915050565b7f496e76616c696420535346000000000000000000000000000000000000000000600082015250565b600061036b600b83610324565b915061037682610335565b602082019050919050565b6000602082019050818103600083015261039a8161035e565b9050919050565b7f496e76616c6964205469636b65744e4654000000000000000000000000000000600082015250565b60006103d7601183610324565b91506103e2826103a1565b602082019050919050565b60006020820190508181036000830152610406816103ca565b9050919050565b611f948061041c6000396000f3fe608060405234801561001057600080fd5b50600436106100f55760003560e01c8063966d1f1d11610097578063cdb6aa3611610066578063cdb6aa3614610292578063dde5b0de146102c2578063f2fde38b146102de578063fb74dabc146102fa576100f5565b8063966d1f1d146101f6578063a4d320cc14610214578063a81d5b6f14610244578063b393391b14610274576100f5565b806374b4c536116100d357806374b4c53614610156578063812acf131461018657806382fd5bac146101a25780638da5cb5b146101d8576100f5565b806303988f84146100fa57806331ea1a3914610130578063715018a61461014c575b600080fd5b610114600480360381019061010f919061149a565b610318565b604051610127979695949392919061158e565b60405180910390f35b61014a6004803603810190610145919061149a565b6103a7565b005b61015461058a565b005b610170600480360381019061016b919061149a565b61059e565b60405161017d91906115fd565b60405180910390f35b6101a0600480360381019061019b9190611644565b6105b6565b005b6101bc60048036038101906101b7919061149a565b610a03565b6040516101cf979695949392919061158e565b60405180910390f35b6101e0610aae565b6040516101ed9190611684565b60405180910390f35b6101fe610ad7565b60405161020b91906115fd565b60405180910390f35b61022e6004803603810190610229919061149a565b610ae1565b60405161023b91906116ba565b60405180910390f35b61025e6004803603810190610259919061149a565b610b01565b60405161026b91906115fd565b60405180910390f35b61027c610b1e565b6040516102899190611734565b60405180910390f35b6102ac60048036038101906102a7919061174f565b610b44565b6040516102b991906115fd565b60405180910390f35b6102dc60048036038101906102d7919061149a565b610fc3565b005b6102f860048036038101906102f391906117dc565b61126c565b005b6103026112ef565b60405161030f919061182a565b60405180910390f35b60046020528060005260406000206000915090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060020154908060030154908060040154908060050154908060060160009054906101000a900460ff16905087565b6103af611315565b6000600460008381526020019081526020016000209050600060038111156103da576103d9611517565b5b8160060160009054906101000a900460ff1660038111156103fe576103fd611517565b5b1461043e576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610435906118a2565b60405180910390fd5b60028160060160006101000a81548160ff0219169083600381111561046657610465611517565b5b02179055506000600660008360020154815260200190815260200160002060006101000a81548160ff021916908315150217905550600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd308360000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1684600201546040518463ffffffff1660e01b8152600401610522939291906118c2565b600060405180830381600087803b15801561053c57600080fd5b505af1158015610550573d6000803e3d6000fd5b505050508060020154827faf3855a84ba7ae9060a15c82675adab08caab3cb5ba10b102c3f0dd8279da02160405160405180910390a35050565b610592611315565b61059c6000611393565b565b60056020528060005260406000206000915090505481565b6105be611315565b6000600460008381526020019081526020016000209050600060038111156105e9576105e8611517565b5b8160060160009054906101000a900460ff16600381111561060d5761060c611517565b5b1461064d576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610644906118a2565b60405180910390fd5b8060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16036106df576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016106d690611945565b60405180910390fd5b8060050154421115610726576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161071d906119b1565b60405180910390fd5b828160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060018160060160006101000a81548160ff0219169083600381111561079157610790611517565b5b02179055506000600660008360020154815260200190815260200160002060006101000a81548160ff0219169083151502179055506000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd858460000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1685600301546040518463ffffffff1660e01b815260040161084f939291906118c2565b6020604051808303816000875af115801561086e573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061089291906119fd565b9050806108d4576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016108cb90611a76565b60405180910390fd5b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd308685600201546040518463ffffffff1660e01b8152600401610937939291906118c2565b600060405180830381600087803b15801561095157600080fd5b505af1158015610965573d6000803e3d6000fd5b505050508160000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff16847f38d1d351aaed4d042e96c9e1ebf794cafeb9ec533318564d969b5335b2d2c321856002015486600301546040516109f5929190611a96565b60405180910390a450505050565b600080600080600080600080600460008a815260200190815260200160002090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff168160010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1682600201548360030154846004015485600501548660060160009054906101000a900460ff16975097509750975097509750975050919395979092949650565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b6000600354905090565b60066020528060005260406000206000915054906101000a900460ff1681565b600060056000838152602001908152602001600020549050919050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000610b4e611315565b600073ffffffffffffffffffffffffffffffffffffffff168773ffffffffffffffffffffffffffffffffffffffff1603610bbd576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610bb490611b0b565b60405180910390fd5b6006600087815260200190815260200160002060009054906101000a900460ff1615610c1e576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610c1590611b77565b60405180910390fd5b60008511610c61576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610c5890611be3565b60405180910390fd5b428211610ca3576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610c9a90611c4f565b60405180910390fd5b60006127108486610cb49190611c9e565b610cbe9190611d0f565b905080861115610d03576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610cfa90611d8c565b60405180910390fd5b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd89308a6040518463ffffffff1660e01b8152600401610d62939291906118c2565b600060405180830381600087803b158015610d7c57600080fd5b505af1158015610d90573d6000803e3d6000fd5b50505050600060036000815480929190610da990611dac565b9190505590506040518060e001604052808a73ffffffffffffffffffffffffffffffffffffffff168152602001600073ffffffffffffffffffffffffffffffffffffffff16815260200189815260200188815260200187815260200185815260200160006003811115610e1f57610e1e611517565b5b8152506004600083815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160020155606082015181600301556080820151816004015560a0820151816005015560c08201518160060160006101000a81548160ff02191690836003811115610f1657610f15611517565b5b021790555090505080600560008a8152602001908152602001600020819055506001600660008a815260200190815260200160002060006101000a81548160ff021916908315150217905550878973ffffffffffffffffffffffffffffffffffffffff16827fdbed7d56dc9bc6aeb14df6b8c0becfa98acd06d60cc5200f0ca43bc73cc46ae88a88604051610fac929190611a96565b60405180910390a480925050509695505050505050565b610fcb611315565b600060046000838152602001908152602001600020905060006003811115610ff657610ff5611517565b5b8160060160009054906101000a900460ff16600381111561101a57611019611517565b5b1461105a576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401611051906118a2565b60405180910390fd5b806005015442116110a0576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161109790611e40565b60405180910390fd5b60038160060160006101000a81548160ff021916908360038111156110c8576110c7611517565b5b02179055506000600660008360020154815260200190815260200160002060006101000a81548160ff021916908315150217905550600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd308360000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1684600201546040518463ffffffff1660e01b8152600401611184939291906118c2565b600060405180830381600087803b15801561119e57600080fd5b505af11580156111b2573d6000803e3d6000fd5b505050508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168160010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16837f1b2aa20e5b03b93ea6a21155a0f4eada25927b7a22cb029fe1fb842ab09cca96846002015460405161126091906115fd565b60405180910390a45050565b611274611315565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16036112e3576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016112da90611ed2565b60405180910390fd5b6112ec81611393565b50565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b61131d611457565b73ffffffffffffffffffffffffffffffffffffffff1661133b610aae565b73ffffffffffffffffffffffffffffffffffffffff1614611391576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161138890611f3e565b60405180910390fd5b565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508173ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a35050565b600033905090565b600080fd5b6000819050919050565b61147781611464565b811461148257600080fd5b50565b6000813590506114948161146e565b92915050565b6000602082840312156114b0576114af61145f565b5b60006114be84828501611485565b91505092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b60006114f2826114c7565b9050919050565b611502816114e7565b82525050565b61151181611464565b82525050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602160045260246000fd5b6004811061155757611556611517565b5b50565b600081905061156882611546565b919050565b60006115788261155a565b9050919050565b6115888161156d565b82525050565b600060e0820190506115a3600083018a6114f9565b6115b060208301896114f9565b6115bd6040830188611508565b6115ca6060830187611508565b6115d76080830186611508565b6115e460a0830185611508565b6115f160c083018461157f565b98975050505050505050565b60006020820190506116126000830184611508565b92915050565b611621816114e7565b811461162c57600080fd5b50565b60008135905061163e81611618565b92915050565b6000806040838503121561165b5761165a61145f565b5b60006116698582860161162f565b925050602061167a85828601611485565b9150509250929050565b600060208201905061169960008301846114f9565b92915050565b60008115159050919050565b6116b48161169f565b82525050565b60006020820190506116cf60008301846116ab565b92915050565b6000819050919050565b60006116fa6116f56116f0846114c7565b6116d5565b6114c7565b9050919050565b600061170c826116df565b9050919050565b600061171e82611701565b9050919050565b61172e81611713565b82525050565b60006020820190506117496000830184611725565b92915050565b60008060008060008060c0878903121561176c5761176b61145f565b5b600061177a89828a0161162f565b965050602061178b89828a01611485565b955050604061179c89828a01611485565b94505060606117ad89828a01611485565b93505060806117be89828a01611485565b92505060a06117cf89828a01611485565b9150509295509295509295565b6000602082840312156117f2576117f161145f565b5b60006118008482850161162f565b91505092915050565b600061181482611701565b9050919050565b61182481611809565b82525050565b600060208201905061183f600083018461181b565b92915050565b600082825260208201905092915050565b7f4e6f74206177616974696e672062757965720000000000000000000000000000600082015250565b600061188c601283611845565b915061189782611856565b602082019050919050565b600060208201905081810360008301526118bb8161187f565b9050919050565b60006060820190506118d760008301866114f9565b6118e460208301856114f9565b6118f16040830184611508565b949350505050565b7f43616e6e6f7420627579206f776e207469636b65740000000000000000000000600082015250565b600061192f601583611845565b915061193a826118f9565b602082019050919050565b6000602082019050818103600083015261195e81611922565b9050919050565b7f4465616c20657870697265640000000000000000000000000000000000000000600082015250565b600061199b600c83611845565b91506119a682611965565b602082019050919050565b600060208201905081810360008301526119ca8161198e565b9050919050565b6119da8161169f565b81146119e557600080fd5b50565b6000815190506119f7816119d1565b92915050565b600060208284031215611a1357611a1261145f565b5b6000611a21848285016119e8565b91505092915050565b7f535346207472616e73666572206661696c656400000000000000000000000000600082015250565b6000611a60601383611845565b9150611a6b82611a2a565b602082019050919050565b60006020820190508181036000830152611a8f81611a53565b9050919050565b6000604082019050611aab6000830185611508565b611ab86020830184611508565b9392505050565b7f496e76616c69642073656c6c6572000000000000000000000000000000000000600082015250565b6000611af5600e83611845565b9150611b0082611abf565b602082019050919050565b60006020820190508181036000830152611b2481611ae8565b9050919050565b7f416c726561647920657363726f77656400000000000000000000000000000000600082015250565b6000611b61601083611845565b9150611b6c82611b2b565b602082019050919050565b60006020820190508181036000830152611b9081611b54565b9050919050565b7f5072696365206d757374206265203e2030000000000000000000000000000000600082015250565b6000611bcd601183611845565b9150611bd882611b97565b602082019050919050565b60006020820190508181036000830152611bfc81611bc0565b9050919050565b7f446561646c696e65206d75737420626520667574757265000000000000000000600082015250565b6000611c39601783611845565b9150611c4482611c03565b602082019050919050565b60006020820190508181036000830152611c6881611c2c565b9050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000611ca982611464565b9150611cb483611464565b9250828202611cc281611464565b91508282048414831517611cd957611cd8611c6f565b5b5092915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601260045260246000fd5b6000611d1a82611464565b9150611d2583611464565b925082611d3557611d34611ce0565b5b828204905092915050565b7f4578636565647320726573616c65206361700000000000000000000000000000600082015250565b6000611d76601283611845565b9150611d8182611d40565b602082019050919050565b60006020820190508181036000830152611da581611d69565b9050919050565b6000611db782611464565b91507fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203611de957611de8611c6f565b5b600182019050919050565b7f4e6f742065787069726564207965740000000000000000000000000000000000600082015250565b6000611e2a600f83611845565b9150611e3582611df4565b602082019050919050565b60006020820190508181036000830152611e5981611e1d565b9050919050565b7f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008201527f6464726573730000000000000000000000000000000000000000000000000000602082015250565b6000611ebc602683611845565b9150611ec782611e60565b604082019050919050565b60006020820190508181036000830152611eeb81611eaf565b9050919050565b7f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572600082015250565b6000611f28602083611845565b9150611f3382611ef2565b602082019050919050565b60006020820190508181036000830152611f5781611f1b565b905091905056fea264697066735822122029bb1d0584a23c964c0b0f59038301d7e962bdc5aa26d345efdb8b56dce0374b64736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_BUYANDSETTLE = "buyAndSettle";

    public static final String FUNC_CANCELDEAL = "cancelDeal";

    public static final String FUNC_CREATEDEAL = "createDeal";

    public static final String FUNC_DEALS = "deals";

    public static final String FUNC_GETDEAL = "getDeal";

    public static final String FUNC_GETDEALBYTICKET = "getDealByTicket";

    public static final String FUNC_ISESCROWED = "isEscrowed";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_REFUNDEXPIREDDEAL = "refundExpiredDeal";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SSFTOKEN = "ssfToken";

    public static final String FUNC_TICKETDEAL = "ticketDeal";

    public static final String FUNC_TICKETNFT = "ticketNFT";

    public static final String FUNC_TOTALDEALS = "totalDeals";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event DEALCANCELLED_EVENT = new Event("DealCancelled",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>(true) {
        }));;

    public static final Event DEALCREATED_EVENT = new Event("DealCreated",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event DEALEXPIREDREFUND_EVENT = new Event("DealExpiredRefund",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event DEALSETTLED_EVENT = new Event("DealSettled",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    @Deprecated
    protected Escrow(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Escrow(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Escrow(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Escrow(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DealCancelledEventResponse> getDealCancelledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALCANCELLED_EVENT,
            transactionReceipt);
        ArrayList<DealCancelledEventResponse> responses = new ArrayList<DealCancelledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealCancelledEventResponse typedResponse = new DealCancelledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealCancelledEventResponse getDealCancelledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALCANCELLED_EVENT, log);
        DealCancelledEventResponse typedResponse = new DealCancelledEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DealCancelledEventResponse> dealCancelledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealCancelledEventFromLog(log));
    }

    public Flowable<DealCancelledEventResponse> dealCancelledEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALCANCELLED_EVENT));
        return dealCancelledEventFlowable(filter);
    }

    public static List<DealCreatedEventResponse> getDealCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALCREATED_EVENT,
            transactionReceipt);
        ArrayList<DealCreatedEventResponse> responses = new ArrayList<DealCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealCreatedEventResponse typedResponse = new DealCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealCreatedEventResponse getDealCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALCREATED_EVENT, log);
        DealCreatedEventResponse typedResponse = new DealCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DealCreatedEventResponse> dealCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealCreatedEventFromLog(log));
    }

    public Flowable<DealCreatedEventResponse> dealCreatedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALCREATED_EVENT));
        return dealCreatedEventFlowable(filter);
    }

    public static List<DealExpiredRefundEventResponse> getDealExpiredRefundEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALEXPIREDREFUND_EVENT,
            transactionReceipt);
        ArrayList<DealExpiredRefundEventResponse> responses = new ArrayList<DealExpiredRefundEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealExpiredRefundEventResponse typedResponse = new DealExpiredRefundEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealExpiredRefundEventResponse getDealExpiredRefundEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALEXPIREDREFUND_EVENT, log);
        DealExpiredRefundEventResponse typedResponse = new DealExpiredRefundEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<DealExpiredRefundEventResponse> dealExpiredRefundEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealExpiredRefundEventFromLog(log));
    }

    public Flowable<DealExpiredRefundEventResponse> dealExpiredRefundEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALEXPIREDREFUND_EVENT));
        return dealExpiredRefundEventFlowable(filter);
    }

    public static List<DealSettledEventResponse> getDealSettledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALSETTLED_EVENT,
            transactionReceipt);
        ArrayList<DealSettledEventResponse> responses = new ArrayList<DealSettledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealSettledEventResponse typedResponse = new DealSettledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealSettledEventResponse getDealSettledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALSETTLED_EVENT, log);
        DealSettledEventResponse typedResponse = new DealSettledEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DealSettledEventResponse> dealSettledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealSettledEventFromLog(log));
    }

    public Flowable<DealSettledEventResponse> dealSettledEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALSETTLED_EVENT));
        return dealSettledEventFlowable(filter);
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

    public RemoteFunctionCall<TransactionReceipt> buyAndSettle(String buyer, BigInteger dealId) {
        final Function function = new Function(FUNC_BUYANDSETTLE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, buyer),
                new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> cancelDeal(BigInteger dealId) {
        final Function function = new Function(FUNC_CANCELDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createDeal(String seller, BigInteger ticketId, BigInteger ssfAmount,
        BigInteger originalPrice, BigInteger resaleCapBps, BigInteger deadline) {
        final Function function = new Function(FUNC_CREATEDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, seller),
                new org.web3j.abi.datatypes.generated.Uint256(ticketId),
                new org.web3j.abi.datatypes.generated.Uint256(ssfAmount),
                new org.web3j.abi.datatypes.generated.Uint256(originalPrice),
                new org.web3j.abi.datatypes.generated.Uint256(resaleCapBps),
                new org.web3j.abi.datatypes.generated.Uint256(deadline)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> deals(
        BigInteger param0) {
        final Function function = new Function(FUNC_DEALS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint8>() {
            }));
        return new RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(
            function,
            new Callable<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                @Override
                public Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call()
                    throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                        (String) results.get(0).getValue(), (String) results.get(1).getValue(),
                        (BigInteger) results.get(2).getValue(), (BigInteger) results.get(3).getValue(),
                        (BigInteger) results.get(4).getValue(), (BigInteger) results.get(5).getValue(),
                        (BigInteger) results.get(6).getValue());
                }
            });
    }

    public RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> getDeal(
        BigInteger dealId) {
        final Function function = new Function(FUNC_GETDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint8>() {
            }));
        return new RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(
            function,
            new Callable<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                @Override
                public Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call()
                    throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                        (String) results.get(0).getValue(), (String) results.get(1).getValue(),
                        (BigInteger) results.get(2).getValue(), (BigInteger) results.get(3).getValue(),
                        (BigInteger) results.get(4).getValue(), (BigInteger) results.get(5).getValue(),
                        (BigInteger) results.get(6).getValue());
                }
            });
    }

    public RemoteFunctionCall<BigInteger> getDealByTicket(BigInteger ticketId) {
        final Function function = new Function(FUNC_GETDEALBYTICKET,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(ticketId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Boolean> isEscrowed(BigInteger param0) {
        final Function function = new Function(FUNC_ISESCROWED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> refundExpiredDeal(BigInteger dealId) {
        final Function function = new Function(FUNC_REFUNDEXPIREDDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> ssfToken() {
        final Function function = new Function(FUNC_SSFTOKEN, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> ticketDeal(BigInteger param0) {
        final Function function = new Function(FUNC_TICKETDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> ticketNFT() {
        final Function function = new Function(FUNC_TICKETNFT, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalDeals() {
        final Function function = new Function(FUNC_TOTALDEALS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Escrow load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new Escrow(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Escrow load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new Escrow(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Escrow load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new Escrow(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Escrow load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new Escrow(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Escrow> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    public static RemoteCall<Escrow> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Escrow> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Escrow> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(),
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

    public static class DealCancelledEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public BigInteger ticketId;
    }

    public static class DealCreatedEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public String seller;

        public BigInteger ticketId;

        public BigInteger ssfAmount;

        public BigInteger deadline;
    }

    public static class DealExpiredRefundEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public String buyer;

        public String seller;

        public BigInteger ticketId;
    }

    public static class DealSettledEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public String buyer;

        public String seller;

        public BigInteger ticketId;

        public BigInteger ssfAmount;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }
}
